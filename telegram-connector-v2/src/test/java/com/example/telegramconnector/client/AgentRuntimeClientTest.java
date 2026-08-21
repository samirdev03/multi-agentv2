package com.example.telegramconnector.client;

import com.example.telegramconnector.config.TelegramConnectorProperties;
import com.example.telegramconnector.domain.TelegramMessage;
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
 * Gleiche Stub-ExchangeFunction-Technik wie {@link TelegramBotRegistrationClientTest}: kein
 * echter agent-runtime in dieser Umgebung verfuegbar, daher wird die Transportschicht ersetzt, um
 * die aufgeloeste Anfrage zu inspizieren. Anders als dort traegt diese Anfrage einen JSON-Body,
 * der zusaetzlich ueber einen {@link MockClientHttpRequest} materialisiert wird, um den
 * tatsaechlich serialisierten Wire-Vertrag zu pruefen (v3.0: /api/v1/messages inkl. channelType).
 */
class AgentRuntimeClientTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void sendAsync_postsToV1MessagesEndpointWithChannelTypeAndMessage() throws Exception {
        // Given
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        WebClient.Builder stubbedBuilder = WebClient.builder()
                .exchangeFunction(request -> {
                    capturedRequest.set(request);
                    return Mono.just(ClientResponse.create(HttpStatus.ACCEPTED).build());
                });
        TelegramConnectorProperties properties =
                new TelegramConnectorProperties("http://agent-runtime.internal", "https://public.example.com");
        AgentRuntimeClient client = new AgentRuntimeClient(stubbedBuilder, properties);

        TelegramMessage message = new TelegramMessage("Hallo Welt", "test-channel-123");

        // When
        client.sendAsync(message).block();

        // Then
        ClientRequest request = capturedRequest.get();
        assertThat(request).isNotNull();
        assertThat(request.method()).isEqualTo(HttpMethod.POST);

        UriComponents uri = UriComponentsBuilder.fromUri(request.url()).build();
        assertThat(uri.getHost()).isEqualTo("agent-runtime.internal");
        assertThat(uri.getPath()).isEqualTo("/api/v1/messages");

        Map<String, Object> body = OBJECT_MAPPER.readValue(extractBody(request), Map.class);
        assertThat(body)
                .containsEntry("channelId", "test-channel-123")
                .containsEntry("channelType", "TELEGRAM")
                .containsEntry("message", "Hallo Welt");
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
