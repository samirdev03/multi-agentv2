package org.example.llm.client.channel;

import org.example.api.dto.ChannelType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ChannelRegistry {

    private final Map<ChannelType, Channel> channels;

    public ChannelRegistry(List<Channel> channels) {
        this.channels = channels.stream()
                .collect(Collectors.toMap(
                        Channel::getType,
                        Function.identity()
                ));
    }

    public Channel getChannel(ChannelType type) {

        Channel channel = channels.get(type);

        if (channel == null) {
            throw new IllegalArgumentException(
                    "Kein Channel für " + type + " registriert"
            );
        }

        return channel;
    }
}