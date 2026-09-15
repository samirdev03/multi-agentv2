package com.example.telegramconnector.domain;

public record TelegramMessage(String message, String channelId, Long telegramChatId, Long updateId) {

    public TelegramMessage(String message, String channelId) { this(message, channelId, null, null); }

    public TelegramMessage {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message darf nicht leer sein");
        }
        if (channelId == null || channelId.isBlank()) {
            throw new IllegalArgumentException("channelId darf nicht leer sein");
        }
    }
}
