package com.example.telegramconnector.domain;

public record TelegramMessage(String message, String channelId) {

    public TelegramMessage {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message darf nicht leer sein");
        }
        if (channelId == null || channelId.isBlank()) {
            throw new IllegalArgumentException("channelId darf nicht leer sein");
        }
    }
}
