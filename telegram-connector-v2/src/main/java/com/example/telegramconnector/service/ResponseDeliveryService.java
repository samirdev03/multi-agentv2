package com.example.telegramconnector.service;

import com.example.telegramconnector.client.TelegramBotClient;
import com.example.telegramconnector.api.FileAttachmentRequest;
import com.example.telegramconnector.api.FileType;
import com.example.telegramconnector.domain.TelegramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResponseDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(ResponseDeliveryService.class);

    private final TelegramChannelResolver channelResolver;
    private final TelegramBotClient telegramBotClient;

    public ResponseDeliveryService(TelegramChannelResolver channelResolver, TelegramBotClient telegramBotClient) {
        this.channelResolver = channelResolver;
        this.telegramBotClient = telegramBotClient;
    }

    public void deliver(String channelId, String message) {
        deliver(channelId, message, List.of());
    }

    public void deliver(String channelId, String message, List<FileAttachmentRequest> attachments) {
        TelegramChannel channel = channelResolver.resolveChannel(channelId);

        List<Mono<Void>> deliveries = new ArrayList<>();
        deliveries.add(withRetry(telegramBotClient.sendMessage(channel, message)));
        for (FileAttachmentRequest attachment : attachments == null ? List.<FileAttachmentRequest>of() : attachments) {
            deliveries.add(withRetry(deliverAttachment(channel, attachment)));
        }

        Flux.concat(deliveries)
                .doOnError(error -> log.error(
                        "Zustellung an Telegram fehlgeschlagen fuer channelId={}", channelId, error))
                .subscribe();
    }

    private Mono<Void> deliverAttachment(TelegramChannel channel, FileAttachmentRequest attachment) {
        return switch (attachment.type()) {
            case IMAGE -> telegramBotClient.sendPhoto(channel, attachment);
            case PDF, TEXT, FILE -> telegramBotClient.sendDocument(channel, attachment);
        };
    }

    private Mono<Void> withRetry(Mono<Void> delivery) {
        return delivery.retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                .filter(this::isTransientError));
    }

    private boolean isTransientError(Throwable error) {
        if (error instanceof WebClientResponseException responseException) {
            return !responseException.getStatusCode().is4xxClientError();
        }
        return true;
    }
}
