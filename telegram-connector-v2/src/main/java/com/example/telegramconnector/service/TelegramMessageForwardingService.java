package com.example.telegramconnector.service;

import com.example.telegramconnector.client.AgentRuntimeClient;
import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.domain.TelegramMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TelegramMessageForwardingService {

    private static final Logger log = LoggerFactory.getLogger(TelegramMessageForwardingService.class);

    private final AgentRuntimeClient agentRuntimeClient;

    public TelegramMessageForwardingService(AgentRuntimeClient agentRuntimeClient) {
        this.agentRuntimeClient = agentRuntimeClient;
    }

    public void forward(TelegramChannel channel, String rawText) {
        TelegramMessage message = new TelegramMessage(rawText, channel.getChannelId());
        agentRuntimeClient.sendAsync(message)
                .doOnError(error -> log.error(
                        "Weiterleitung an agent-runtime fehlgeschlagen fuer channelId={}",
                        channel.getChannelId(), error))
                .subscribe();
    }
}
