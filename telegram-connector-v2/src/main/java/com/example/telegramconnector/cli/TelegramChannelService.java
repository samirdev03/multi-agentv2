package com.example.telegramconnector.cli;

import com.example.telegramconnector.client.TelegramBotRegistrationClient;
import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.repository.TelegramChannelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelegramChannelService {

    private final TelegramChannelRepository channelRepository;
    private final TelegramBotRegistrationClient registrationClient;

    public TelegramChannelService(
            TelegramChannelRepository channelRepository,
            TelegramBotRegistrationClient registrationClient) {
        this.channelRepository = channelRepository;
        this.registrationClient = registrationClient;
    }

    public TelegramChannel addChannel(
            String channelId,
            String name,
            String botToken) {

        if (channelRepository.existsById(channelId)) {
            throw new IllegalArgumentException(
                    "Channel '" + channelId + "' existiert bereits."
            );
        }

        TelegramChannel channel =
                new TelegramChannel(channelId, name, botToken);

        channelRepository.save(channel);

        registrationClient.registerWebhook(channelId, botToken);

        return channel;
    }

    public List<TelegramChannel> listChannels() {
        return channelRepository.findAll();
    }
}