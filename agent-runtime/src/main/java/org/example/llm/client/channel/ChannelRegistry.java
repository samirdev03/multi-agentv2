package org.example.llm.client.channel;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChannelRegistry {
    public final Map<String, TelegramChannel> channels = new HashMap<>();
    private TelegramChannel telegram;

    @PostConstruct
    void init(){
        channels.put("telegram", telegram);
    }
}

