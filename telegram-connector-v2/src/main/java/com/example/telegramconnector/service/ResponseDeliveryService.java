package com.example.telegramconnector.service;

import com.example.telegramconnector.client.TelegramBotClient;
import com.example.telegramconnector.domain.TelegramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
        TelegramChannel channel = channelResolver.resolveChannel(channelId);

        telegramBotClient.sendMessage(channel, message)
                .doOnError(error -> log.error(
                        "Zustellung an Telegram fehlgeschlagen fuer channelId={}", channelId, error))
                .subscribe();
    }
}
