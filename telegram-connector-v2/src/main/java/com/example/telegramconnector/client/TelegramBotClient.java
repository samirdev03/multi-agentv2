package com.example.telegramconnector.client;

import com.example.telegramconnector.api.FileAttachmentRequest;
import com.example.telegramconnector.domain.TelegramChannel;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.http.client.MultipartBodyBuilder;
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

    public Mono<Void> sendPhoto(TelegramChannel channel, FileAttachmentRequest attachment) {
        return sendAttachment(channel, attachment, "sendPhoto", "photo");
    }

    public Mono<Void> sendDocument(TelegramChannel channel, FileAttachmentRequest attachment) {
        return sendAttachment(channel, attachment, "sendDocument", "document");
    }

    private Mono<Void> sendAttachment(
            TelegramChannel channel,
            FileAttachmentRequest attachment,
            String endpoint,
            String fileField) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("chat_id", channel.getChannelId());
        body.part(fileField, new FileSystemResource(attachment.path()))
                .filename(attachment.fileName());

        return webClient.post()
                .uri("/bot{botToken}/" + endpoint, channel.getBotToken())
                .bodyValue(body.build())
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    private record SendMessageRequest(@JsonProperty("chat_id") String chatId, String text) {
    }
}
