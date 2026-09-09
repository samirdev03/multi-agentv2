package org.example.llm.client.channel;

import org.example.api.dto.TelegramMessageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TelegramChannel implements Channel{

    private final RestClient restClient;

    public TelegramChannel(
            @Value("${telegram.connector.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void sendMessage(TelegramMessageDto message) {

        restClient.post()
                .uri("/api/v1/responses")
                .body(message)
                .retrieve()
                .toBodilessEntity();
    }
}