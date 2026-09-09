package org.example.api.dto;

public record TelegramMessageDto(String message, String channelId) {
    public TelegramMessageDto {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message darf nicht leer sein");
        }
        if (channelId == null || channelId.isBlank()) {
            throw new IllegalArgumentException("channelId darf nicht leer sein");
        }
    }
}
