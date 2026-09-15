package org.example.web.dto;

import java.net.URI;
import java.util.UUID;

public record GenericRequestDto(
        ChannelType channelType,
        String channelId,
        String content,
        URI responseUrl,
        UUID requestId
) implements RequestDto {

    public GenericRequestDto {
        requestId = requestId == null ? UUID.randomUUID() : requestId;
    }

    public GenericRequestDto(ChannelType channelType, String channelId, String content, URI responseUrl) {
        this(channelType, channelId, content, responseUrl, UUID.randomUUID());
    }

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
