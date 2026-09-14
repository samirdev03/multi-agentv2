package org.example.api.dto;

import java.util.List;

public interface ResponseDto {

    ChannelType getChannelType();

    String getChannelId();

    String getContent();

    List<FileAttachmentDto> getAttachments();
}