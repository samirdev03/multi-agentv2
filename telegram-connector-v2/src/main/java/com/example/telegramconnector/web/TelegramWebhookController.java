package com.example.telegramconnector.web;

import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.service.TelegramChannelResolver;
import com.example.telegramconnector.service.TelegramMessageForwardingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class TelegramWebhookController {

    private final TelegramChannelResolver channelResolver;
    private final TelegramMessageForwardingService forwardingService;

    public TelegramWebhookController(TelegramChannelResolver channelResolver,
                                      TelegramMessageForwardingService forwardingService) {
        this.channelResolver = channelResolver;
        this.forwardingService = forwardingService;
    }

    @PostMapping("/webhook/{channelId}")
    public ResponseEntity<Void> receiveUpdate(@PathVariable("channelId") String channelId,
                                               @RequestBody Update update) {
        TelegramChannel channel = channelResolver.resolveChannel(channelId);

        String text = extractText(update);
        if (text != null) {
            forwardingService.forward(channel, text);
        }
        // Telegram erwartet zuegig eine 200er-Antwort, unabhaengig davon, ob Textinhalt
        // vorhanden war - sonst wiederholt Telegram den Zustellversuch.
        return ResponseEntity.ok().build();
    }

    private String extractText(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return update.getMessage().getText();
        }
        return null;
    }
}
