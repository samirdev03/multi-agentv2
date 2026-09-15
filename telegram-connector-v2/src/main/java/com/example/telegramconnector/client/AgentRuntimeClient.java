package com.example.telegramconnector.client;

import com.example.telegramconnector.config.TelegramConnectorProperties;
import com.example.telegramconnector.domain.ChannelType;
import com.example.telegramconnector.domain.TelegramMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Component
public class AgentRuntimeClient {

    private final WebClient webClient;
    private final String callbackBaseUrl;

    public AgentRuntimeClient(WebClient.Builder webClientBuilder,
                               TelegramConnectorProperties properties) {
        this.webClient = webClientBuilder
                .baseUrl(properties.agentRuntimeBaseUrl())
                .build();
        this.callbackBaseUrl = properties.callbackBaseUrl();
    }

    /**
     * Nicht-blockierender Aufruf an die asynchrone REST-API von agent-runtime.
     * Ab Architektur-Doku v3.0: Pfad /api/v1/messages (vorher /api/messages),
     * Wire-Vertrag IncomingMessageRequest inkl. channelType (vorher TelegramMessage ohne Typ).
     */
    public Mono<Void> sendAsync(TelegramMessage message) {
        UUID requestId = UUID.nameUUIDFromBytes(((message.updateId() == null ? "legacy" : message.updateId()) + ":" + message.channelId()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        IncomingMessageRequest request = new IncomingMessageRequest(
                requestId,
                ChannelType.TELEGRAM,
                message.channelId(),
                message.message(),
                callbackBaseUrl + "/api/v1/responses/" + message.channelId());

        return webClient.post()
                .uri("/api/v1/messages")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    private record IncomingMessageRequest(
            UUID requestId,
            ChannelType channelType,
            String channelId,
            String content,
            String responseUrl) {
    }
}
