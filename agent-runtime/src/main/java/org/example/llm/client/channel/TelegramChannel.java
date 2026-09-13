package org.example.llm.client.channel;

import org.example.api.dto.TelegramMessageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TelegramChannel implements Channel{

    private final RestClient restClient;

    public TelegramChannel(
            @Value("${telegram.connector.base-url}") String baseUrl
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(15_000);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
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