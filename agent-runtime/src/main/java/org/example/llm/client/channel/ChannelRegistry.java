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

    /**
     * Returns the Telegram channel client. Access it only through this method, never directly via
     * the {@code channels} field: Beans in {@code org.example..*} are CGLIB-proxied by the
     * {@code LoggingAspect}. For classes without a no-arg constructor the proxy does not run field
     * initializers, so direct access to a public field yields {@code null}.
     */
    public TelegramChannel getTelegram() {
        return channels.get("telegram");
    }
}