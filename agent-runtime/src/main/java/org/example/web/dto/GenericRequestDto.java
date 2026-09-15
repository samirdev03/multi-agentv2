package org.example.web.dto;

import java.net.URI;
import java.util.UUID;
import java.util.List;

public record GenericRequestDto(
        ChannelType channelType,
        String channelId,
        String content,
        URI responseUrl,
        UUID requestId,
        List<IncomingFileDto> files
) implements RequestDto {

    public GenericRequestDto {
        requestId = requestId == null ? UUID.randomUUID() : requestId;
        files = files == null ? List.of() : List.copyOf(files);
    }

    public GenericRequestDto(ChannelType channelType, String channelId, String content, URI responseUrl) {
        this(channelType, channelId, content, responseUrl, UUID.randomUUID(), List.of());
    }

    public GenericRequestDto(ChannelType channelType, String channelId, String content, URI responseUrl, UUID requestId) {
        this(channelType, channelId, content, responseUrl, requestId, List.of());
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
