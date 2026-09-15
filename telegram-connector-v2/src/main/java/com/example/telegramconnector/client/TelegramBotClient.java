package com.example.telegramconnector.client;

import com.example.telegramconnector.api.FileAttachmentRequest;
import com.example.telegramconnector.domain.TelegramChannel;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.io.ByteArrayResource;
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
        return sendMessage(channel, channel.getChannelId(), text);
    }

    public Mono<Void> sendMessage(TelegramChannel channel, String chatId, String text) {
        SendMessageRequest request = new SendMessageRequest(chatId, text);

        return webClient.post()
                .uri("/bot{botToken}/sendMessage", channel.getBotToken())
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    public Mono<Void> sendPhoto(TelegramChannel channel, FileAttachmentRequest attachment) {
        return sendPhoto(channel, channel.getChannelId(), attachment);
    }

    public Mono<Void> sendPhoto(TelegramChannel channel, String chatId, FileAttachmentRequest attachment) {
        return sendAttachment(channel, chatId, attachment, "sendPhoto", "photo");
    }

    public Mono<Void> sendDocument(TelegramChannel channel, FileAttachmentRequest attachment) {
        return sendDocument(channel, channel.getChannelId(), attachment);
    }

    public Mono<Void> sendDocument(TelegramChannel channel, String chatId, FileAttachmentRequest attachment) {
        return sendAttachment(channel, chatId, attachment, "sendDocument", "document");
    }

    private Mono<Void> sendAttachment(
            TelegramChannel channel,
            String chatId,
            FileAttachmentRequest attachment,
            String endpoint,
            String fileField) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("chat_id", chatId);
        body.part(fileField, new ByteArrayResource(attachment.content()) {
                    @Override
                    public String getFilename() {
                        return attachment.fileName();
                    }
                })
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
