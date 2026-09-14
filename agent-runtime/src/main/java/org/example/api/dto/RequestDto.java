package org.example.api.dto;

import java.net.URI;

public interface RequestDto {

    ChannelType getChannelType();

    String getChannelId();

    String getContent();

    URI responseUrl();
}
