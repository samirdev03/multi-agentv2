package org.example.api.dto;

public record TelegramRequestDto(
        String message,
        String channelId
) implements RequestDto {

    @Override
    public ChannelType getChannelType() {
        return ChannelType.TELEGRAM;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public String getContent() {
        return message;
    }
}