package com.example.telegramconnector.client;

import com.example.telegramconnector.domain.TelegramChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gleiche Stub-ExchangeFunction-Technik wie {@link TelegramBotRegistrationClientTest}, hier
 * zusaetzlich mit Body-Pruefung (siehe {@link AgentRuntimeClientTest} fuer den gleichen Ansatz).
 */
class TelegramBotClientTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void sendMessage_postsToTelegramSendMessageWithChatIdAndText() throws Exception {
        // Given
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        WebClient.Builder stubbedBuilder = WebClient.builder()
                .exchangeFunction(request -> {
                    capturedRequest.set(request);
                    return Mono.just(ClientResponse.create(HttpStatus.OK).build());
                });
        TelegramBotClient client = new TelegramBotClient(stubbedBuilder);

        TelegramChannel channel = new TelegramChannel("test-channel-123", "Test Channel", "bot-token-123");

        // When
        client.sendMessage(channel, "Hallo zurueck").block();

        // Then
        ClientRequest request = capturedRequest.get();
        assertThat(request).isNotNull();
        assertThat(request.method()).isEqualTo(HttpMethod.POST);

        UriComponents uri = UriComponentsBuilder.fromUri(request.url()).build();
        assertThat(uri.getScheme()).isEqualTo("https");
        assertThat(uri.getHost()).isEqualTo("api.telegram.org");
        assertThat(uri.getPath()).isEqualTo("/bot" + channel.getBotToken() + "/sendMessage");

        Map<String, Object> body = OBJECT_MAPPER.readValue(extractBody(request), Map.class);
        assertThat(body)
                .containsEntry("chat_id", channel.getChannelId())
                .containsEntry("text", "Hallo zurueck");
    }

    private static String extractBody(ClientRequest request) {
        MockClientHttpRequest httpRequest = new MockClientHttpRequest(HttpMethod.POST, URI.create("http://test"));
        request.body().insert(httpRequest, new BodyInserter.Context() {
            @Override
            public List<HttpMessageWriter<?>> messageWriters() {
                return ExchangeStrategies.withDefaults().messageWriters();
            }

            @Override
            public Optional<ServerHttpRequest> serverRequest() {
                return Optional.empty();
            }

            @Override
            public Map<String, Object> hints() {
                return Collections.emptyMap();
            }
        }).block();
        return httpRequest.getBodyAsString().block();
    }
}
