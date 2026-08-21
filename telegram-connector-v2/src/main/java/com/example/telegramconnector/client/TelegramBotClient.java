package com.example.telegramconnector.client;

import com.example.telegramconnector.domain.TelegramChannel;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TelegramBotClient {

    private static final String TELEGRAM_API_BASE_URL = "https://api.telegram.org";

    private final WebClient webClient;

    public TelegramBotClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(TELEGRAM_API_BASE_URL).build();
    }

    public Mono<Void> sendMessage(TelegramChannel channel, String text) {
        SendMessageRequest request = new SendMessageRequest(channel.getChannelId(), text);

        return webClient.post()
                .uri("/bot{botToken}/sendMessage", channel.getBotToken())
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    private record SendMessageRequest(@JsonProperty("chat_id") String chatId, String text) {
    }
}
