package com.example.telegramconnector.service.exception;

public class ChannelNotFoundException extends RuntimeException {

    private final String channelId;

    public ChannelNotFoundException(String channelId) {
        super("Kein TelegramChannel mit channelId '" + channelId + "' registriert");
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }
}
