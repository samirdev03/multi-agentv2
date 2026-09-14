package org.example.api.dto;

import java.util.List;

public record TelegramResponseDto(String channelId, String content, List<FileAttachmentDto> attachments) implements ResponseDto {

    @Override
    public ChannelType getChannelType() {
        return ChannelType.TELEGRAM;
    }

    @Override
    public String getChannelId() {
        return this.channelId;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public List<FileAttachmentDto> getAttachments() {
        return this.attachments;
    }
}
