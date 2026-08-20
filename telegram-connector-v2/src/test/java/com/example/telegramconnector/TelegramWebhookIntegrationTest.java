package com.example.telegramconnector;

import com.example.telegramconnector.client.AgentRuntimeClient;
import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.domain.TelegramMessage;
import com.example.telegramconnector.repository.TelegramChannelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Durchgaengiger Test ueber den echten Spring-Kontext: Webhook-Controller, Channel-Resolver,
 * echte Persistenz (H2 im PostgreSQL-Kompatibilitaetsmodus, siehe src/test/resources/application.yml)
 * und Forwarding-Service muessen zusammenspielen. Nur {@link AgentRuntimeClient} wird per
 * {@code @MockBean} ersetzt, da ein echter agent-runtime-Dienst in dieser Umgebung nicht
 * verfuegbar ist - alle anderen Bestandteile (inkl. Datenbank und HTTP-Schicht) sind real.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TelegramWebhookIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TelegramChannelRepository channelRepository;

    @MockBean
    private AgentRuntimeClient agentRuntimeClient;

    @Test
    void receiveUpdate_withKnownChannelAndTextMessage_returnsOkAndForwardsToAgentRuntime() {
        // Given
        String channelId = "integration-channel-123";
        TelegramChannel channel = new TelegramChannel(channelId, "Integration Channel", "bot-token-123");
        channelRepository.save(channel);
        when(agentRuntimeClient.sendAsync(any())).thenReturn(Mono.empty());

        String updateJson = """
                {
                  "update_id": 1,
                  "message": {
                    "message_id": 1,
                    "date": 1691500000,
                    "chat": { "id": 42, "type": "private" },
                    "text": "Hallo Welt"
                  }
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(updateJson, headers);

        // When
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/webhook/{channelId}", request, Void.class, channelId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // forward(...) ruft sendAsync(...).subscribe() fire-and-forget auf; verify(...,
        // timeout(...)) wartet deterministisch statt sofort zu assertieren oder blind zu schlafen.
        verify(agentRuntimeClient, timeout(2000))
                .sendAsync(eq(new TelegramMessage("Hallo Welt", channelId)));
    }

    @Test
    void receiveUpdate_withUnknownChannelId_returnsNotFoundAndDoesNotCallAgentRuntime() {
        // Given
        String unknownChannelId = "unknown-integration-channel-456";

        String updateJson = """
                {
                  "update_id": 2,
                  "message": {
                    "message_id": 2,
                    "date": 1691500000,
                    "chat": { "id": 42, "type": "private" },
                    "text": "Hallo Welt"
                  }
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(updateJson, headers);

        // When
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/webhook/{channelId}", request, Void.class, unknownChannelId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(agentRuntimeClient, never()).sendAsync(any());
    }
}
