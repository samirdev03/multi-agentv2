package com.example.telegramconnector.client;

import com.example.telegramconnector.config.TelegramConnectorProperties;
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
     */
    public Mono<Void> sendAsync(TelegramMessage message) {
        return webClient.post()
                .uri("/api/messages")
                .bodyValue(message)
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}
