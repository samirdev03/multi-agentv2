package com.example.telegramconnector.domain;

import java.util.List;

public record TelegramMessage(String message, String channelId, Long telegramChatId, Long updateId, List<TelegramFile> files) {

    public TelegramMessage(String message, String channelId) { this(message, channelId, null, null, List.of()); }
    public TelegramMessage(String message, String channelId, Long telegramChatId, Long updateId) { this(message, channelId, telegramChatId, updateId, List.of()); }

    public TelegramMessage {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message darf nicht leer sein");
        }
        if (channelId == null || channelId.isBlank()) {
            throw new IllegalArgumentException("channelId darf nicht leer sein");
        }
        files = files == null ? List.of() : List.copyOf(files);
    }
}
