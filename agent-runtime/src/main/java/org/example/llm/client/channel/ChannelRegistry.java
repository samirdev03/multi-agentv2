package org.example.llm.client.channel;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChannelRegistry {
    public final Map<String, TelegramChannel> channels = new HashMap<>();
    private final TelegramChannel telegram;

    @PostConstruct
    void init(){
        channels.put("telegram", telegram);
    }
}