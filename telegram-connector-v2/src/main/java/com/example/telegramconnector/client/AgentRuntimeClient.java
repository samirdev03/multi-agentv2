package com.example.telegramconnector.client;

import com.example.telegramconnector.config.TelegramConnectorProperties;
import com.example.telegramconnector.domain.ChannelType;
import com.example.telegramconnector.domain.TelegramMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AgentRuntimeClient {

    private final WebClient webClient;

    public AgentRuntimeClient(WebClient.Builder webClientBuilder,
                               TelegramConnectorProperties properties) {
        this.webClient = webClientBuilder
                .baseUrl(properties.agentRuntimeBaseUrl())
                .build();
    }

    /**
     * Nicht-blockierender Aufruf an die asynchrone REST-API von agent-runtime.
     * Ab Architektur-Doku v3.0: Pfad /api/v1/messages (vorher /api/messages),
     * Wire-Vertrag IncomingMessageRequest inkl. channelType (vorher TelegramMessage ohne Typ).
     */
    public Mono<Void> sendAsync(TelegramMessage message) {
        IncomingMessageRequest request = new IncomingMessageRequest(
                message.channelId(), ChannelType.TELEGRAM, message.message());

        return webClient.post()
                .uri("/api/v1/messages")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    private record IncomingMessageRequest(String channelId, ChannelType channelType, String message) {
    }
}
