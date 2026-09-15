package com.example.telegramconnector.service;

import com.example.telegramconnector.client.AgentRuntimeClient;
import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.domain.TelegramMessage;
import com.example.telegramconnector.domain.TelegramFile;
import com.example.telegramconnector.client.TelegramBotClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class TelegramMessageForwardingService {

    private static final Logger log = LoggerFactory.getLogger(TelegramMessageForwardingService.class);

    private final AgentRuntimeClient agentRuntimeClient;
    private final TelegramChatChannelService chatChannels;
    private final TelegramBotClient telegramBotClient;

    public TelegramMessageForwardingService(AgentRuntimeClient agentRuntimeClient, TelegramChatChannelService chatChannels, TelegramBotClient telegramBotClient) {
        this.agentRuntimeClient = agentRuntimeClient;
        this.chatChannels = chatChannels;
        this.telegramBotClient = telegramBotClient;
    }

    public void forward(TelegramChannel channel, String rawText) {
        forward(channel, rawText, null, null);
    }

    public void forward(TelegramChannel channel, String rawText, Long telegramChatId, Long updateId) {
        forward(channel, rawText, telegramChatId, updateId, null, null, null);
    }

    public void forward(TelegramChannel channel, String rawText, Long telegramChatId, Long updateId,
                        String fileId, String fileName, String contentType) {
        String internalChannelId = telegramChatId == null ? channel.getChannelId() : chatChannels.resolve(telegramChatId, channel.getChannelId()).getChannelId();
        var send = fileId == null ? send(channel, internalChannelId, rawText, telegramChatId, updateId, null)
                : telegramBotClient.downloadFile(channel, fileId, fileName, contentType)
                .flatMap(file -> send(channel, internalChannelId, rawText, telegramChatId, updateId, file));
        send.retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                        .filter(this::isTransientError))
                .doOnError(error -> log.error(
                        "Weiterleitung an agent-runtime fehlgeschlagen fuer channelId={}",
                        channel.getChannelId(), error))
                .subscribe();
    }

    private reactor.core.publisher.Mono<Void> send(TelegramChannel channel, String internalChannelId, String rawText,
                                                     Long telegramChatId, Long updateId, TelegramFile file) {
        String text = rawText == null || rawText.isBlank() ? "Der Benutzer hat eine Datei gesendet." : rawText;
        TelegramMessage message = new TelegramMessage(text, internalChannelId, telegramChatId, updateId,
                file == null ? java.util.List.of() : java.util.List.of(file));
        return agentRuntimeClient.sendAsync(message);
    }

    private boolean isTransientError(Throwable error) {
        if (error instanceof WebClientResponseException responseException) {
            return !responseException.getStatusCode().is4xxClientError();
        }
        return true;
    }
}
