package com.example.telegramconnector.cli;

import com.example.telegramconnector.client.TelegramBotRegistrationClient;
import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.repository.TelegramChannelRepository;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class TelegramChannelCliCommands {

    private final TelegramChannelRepository channelRepository;
    private final TelegramBotRegistrationClient registrationClient;

    public TelegramChannelCliCommands(TelegramChannelRepository channelRepository,
                                       TelegramBotRegistrationClient registrationClient) {
        this.channelRepository = channelRepository;
        this.registrationClient = registrationClient;
    }

    @ShellMethod(key = "add-channel",
            value = "Legt einen neuen Telegram-Channel an und registriert den Webhook bei Telegram")
    public String addChannel(
            @ShellOption(help = "Eindeutige, frei waehlbare Channel-ID (Teil des Webhook-Pfads)") String channelId,
            @ShellOption(help = "Anzeigename des Bots") String name,
            @ShellOption(help = "Bot-Token von @BotFather") String botToken) {

        if (channelRepository.existsById(channelId)) {
            return "Channel '" + channelId + "' existiert bereits.";
        }

        TelegramChannel channel = new TelegramChannel(channelId, name, botToken);
        channelRepository.save(channel);
        registrationClient.registerWebhook(channelId, botToken);

        return "Channel '" + channelId + "' angelegt und Webhook bei Telegram registriert.";
    }

    @ShellMethod(key = "list-channels", value = "Listet alle konfigurierten Telegram-Channels auf")
    public String listChannels() {
        var channels = channelRepository.findAll();
        if (channels.isEmpty()) {
            return "Keine Channels konfiguriert.";
        }
        StringBuilder sb = new StringBuilder();
        for (TelegramChannel channel : channels) {
            sb.append(channel.getChannelId()).append(" - ").append(channel.getName())
              .append(System.lineSeparator());
        }
        return sb.toString();
    }
}
