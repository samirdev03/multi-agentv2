package org.example.api.dto;

import java.net.URI;

public record GenericRequestDto(
        ChannelType channelType,
        String channelId,
        String content,
        URI responseUrl
) implements RequestDto {

    @Override
    public ChannelType getChannelType() {
        return channelType;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public String getContent() {
        return content;
    }
}
