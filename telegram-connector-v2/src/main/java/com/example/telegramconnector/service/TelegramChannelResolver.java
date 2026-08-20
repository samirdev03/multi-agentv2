package com.example.telegramconnector.service;

import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.repository.TelegramChannelRepository;
import com.example.telegramconnector.service.exception.ChannelNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TelegramChannelResolver {

    private final TelegramChannelRepository channelRepository;

    public TelegramChannelResolver(TelegramChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    public TelegramChannel resolveChannel(String channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelNotFoundException(channelId));
    }
}
