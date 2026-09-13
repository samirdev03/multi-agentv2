package org.example.e2e;

import org.example.agent.AgentEntity;
import org.example.agent.AgentRepository;
import org.example.api.dto.TelegramMessageDto;
import org.example.llm.client.channel.TelegramChannel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * End-to-end test for the agent-runtime module.
 *
 * Sends real Telegram messages to the runtime API ({@code POST /api/v1/messages}) and verifies
 * that a real AI answer comes back (captured at the {@link TelegramChannel} boundary, which in
 * production POSTs the answer to the telegram-connector under {@code /api/v1/responses}).
 *
 * The OpenRouter LLM backend is intentionally NOT mocked - answers are fetched from the real
 * OpenRouter API using the free model {@code nex-agi/nex-n2.5-mini:free}. The API key is resolved
 * from the environment variable {@code OPENROUTER_API_KEY} or, as a fallback, from the repository's
 * {@code .env} file (same convention as docker-compose). If no key can be resolved, the whole class
 * is skipped.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EnabledIf(value = "isConfigured",
        disabledReason = "OPENROUTER_API_KEY weder als Env-Variable noch in .env gefunden")
class AgentRuntimeE2ETest {

    private static final String FREE_MODEL = "nex-agi/nex-n2.5-mini:free";
    private static final String CONNECTOR_WIRE_CHANNEL_TYPE = "TELEGRAM";

    static {
        // Resolve the key lazily (env var first, then repo .env) and expose it as a system
        // property so ${OPENROUTER_API_KEY} in application.properties resolves when the Spring
        // context loads. Runs at class-load time, i.e. before the context is created.
        String key = System.getenv("OPENROUTER_API_KEY");
        if (key == null || key.isBlank()) {
            key = readApiKeyFromDotEnv();
        }
        if (key != null && !key.isBlank()) {
            System.setProperty("OPENROUTER_API_KEY", key.trim());
        }
    }

    /**
     * Only the delivery boundary is mocked: {@link TelegramChannel} would POST the AI answer to
     * the telegram-connector, which is not running here. Capturing the call lets the test assert
     * that a real LLM answer was produced and carried the correct channelId.
     */
    @MockitoBean
    private TelegramChannel telegramChannel;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    /**
     * Isolate the test from previous runs and from production data: use a fresh temp SQLite DB
     * under target/ instead of the default ./data/agent-runtime.db.
     */
    @DynamicPropertySource
    static void testDatabaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:./target/agent-runtime-e2e.db");
    }

    @BeforeAll
    static void deleteTestDatabase() throws Exception {
        Files.deleteIfExists(Path.of("./target/agent-runtime-e2e.db"));
    }

    /**
     * Condition for {@link EnabledIf}: true when an OpenRouter key is available, either as an
     * environment variable, as a system property (set by the static initializer) or via .env.
     */
    static boolean isConfigured() {
        String key = System.getenv("OPENROUTER_API_KEY");
        if (key == null || key.isBlank()) {
            key = System.getProperty("OPENROUTER_API_KEY");
        }
        if (key == null || key.isBlank()) {
            key = readApiKeyFromDotEnv();
        }
        return key != null && !key.isBlank();
    }

    /**
     * Reads OPENROUTER_API_KEY from the repository .env file (docker-compose convention). Tries the
     * current working directory first, then one level up (repo root), so it works both from the
     * module directory (Maven, IDE) and from a test-launched process.
     */
    private static String readApiKeyFromDotEnv() {
        for (Path candidate : java.util.List.of(Path.of(".env"), Path.of("..", ".env"))) {
            try {
                if (Files.isRegularFile(candidate)) {
                    for (String line : Files.readAllLines(candidate)) {
                        if (line.startsWith("OPENROUTER_API_KEY=")) {
                            return line.substring("OPENROUTER_API_KEY=".length()).trim();
                        }
                    }
                }
            } catch (java.io.IOException ignored) {
                // Try the next candidate.
            }
        }
        return null;
    }

    @Test
    void unregisteredChannelId_createsFallbackAgent_andDeliversRealAiAnswer() throws Exception {
        String channelId = "e2e-unregistered-channel-1";
        long agentsBefore = agentRepository.count();

        ResponseEntity<Void> response = postMessage(channelId,
                "Hallo, das ist eine E2E-Testnachricht für die Agent Runtime.");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        TelegramMessageDto delivered = awaitDeliveredAnswer();
        assertThat(delivered.channelId()).isEqualTo(channelId);
        assertThat(delivered.message()).isNotBlank();
        assertThat(delivered.message().length()).isGreaterThanOrEqualTo(3);

        // Without a registered channelId the runtime must have created and persisted a fallback
        // agent (FALLBACK_PROMPT) instead of failing or dropping the channel.
        AgentEntity fallbackAgent = agentRepository.findByChannelId(channelId).orElseThrow();
        assertThat(fallbackAgent.getSystemPrompt()).contains("Fallback Agent");
        assertThat(agentRepository.count()).isEqualTo(agentsBefore + 1);
    }

    @Test
    void registeredChannelIdAndAgent_usesRegisteredAgent_andDeliversRealAiAnswer() throws Exception {
        String channelId = "e2e-registered-channel-2";
        String customPrompt =
                "Du bist der registrierte E2E-Testagent. Antworte auf Deutsch und bleibe freundlich.";

        AgentEntity registered = AgentEntity.builder()
                .name("Registered E2E Agent")
                .systemPrompt(customPrompt)
                .channelId(channelId)
                .provider("openrouter")
                .modelId(FREE_MODEL)
                .build();
        registered = agentRepository.saveAndFlush(registered);
        long agentsBefore = agentRepository.count();

        ResponseEntity<Void> response = postMessage(channelId, "Teste mich bitte.");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        TelegramMessageDto delivered = awaitDeliveredAnswer();
        assertThat(delivered.channelId()).isEqualTo(channelId);
        assertThat(delivered.message()).isNotBlank();
        assertThat(delivered.message().length()).isGreaterThanOrEqualTo(3);

        // The pre-registered agent must be the one used: same row id, custom prompt intact, and
        // no additional (fallback) agent row created for the already-registered channel.
        AgentEntity loaded = agentRepository.findById(registered.getAgentId()).orElseThrow();
        assertThat(loaded.getSystemPrompt()).isEqualTo(customPrompt);
        assertThat(agentRepository.findByChannelId(channelId).orElseThrow().getSystemPrompt())
                .isEqualTo(customPrompt);
        assertThat(agentRepository.count()).isEqualTo(agentsBefore);
    }

    private ResponseEntity<Void> postMessage(String channelId, String userMessage) throws Exception {
        // Body mirrors the exact wire contract of the telegram-connector's AgentRuntimeClient
        // (IncomingMessageRequest: channelId, channelType, message). The runtime ignores the extra
        // channelType field, exactly as it does in production.
        String body = objectMapper.writeValueAsString(Map.of(
                "channelId", channelId,
                "channelType", CONNECTOR_WIRE_CHANNEL_TYPE,
                "message", userMessage
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        return restTemplate.postForEntity("/api/v1/messages", request, Void.class);
    }

    private TelegramMessageDto awaitDeliveredAnswer() {
        ArgumentCaptor<TelegramMessageDto> captor = ArgumentCaptor.forClass(TelegramMessageDto.class);
        // The runtime flow (LLM call + delivery) is synchronous within the POST /api/v1/messages
        // request; the timeout only guards against slow real LLM responses.
        verify(telegramChannel, timeout(30_000)).sendMessage(captor.capture());
        return captor.getValue();
    }
}