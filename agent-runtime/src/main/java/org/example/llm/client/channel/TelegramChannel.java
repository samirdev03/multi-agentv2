package org.example.llm.client.channel;

import org.example.api.dto.ChannelType;
import org.example.api.dto.ResponseDto;
import org.example.api.dto.TelegramResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TelegramChannel implements Channel {

    private final RestClient restClient;

    public TelegramChannel(
            @Value("${telegram.connector.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public ChannelType getType() {
        return ChannelType.TELEGRAM;
    }

    @Override
    public ResponseDto buildResponse(
            String content,
            String channelId
    ) {
        return new TelegramResponseDto(
                content,
                channelId,
                List.of()
        );
    }

    @Override
    public void sendResponse(ResponseDto response) {

        restClient.post()
                .uri("/api/v1/responses")
                .body(response)
                .retrieve()
                .toBodilessEntity();
    }
}