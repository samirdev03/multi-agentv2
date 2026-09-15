package com.example.telegramconnector.web;

import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.service.TelegramChannelResolver;
import com.example.telegramconnector.service.TelegramMessageForwardingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
                                               @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretToken,
                                               @RequestBody Update update) {
        TelegramChannel channel = channelResolver.resolveChannel(channelId);

        if (channel.getSecretToken() != null && !channel.getSecretToken().equals(secretToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String text = extractText(update);
        if (text != null) {
            Long chatId = update.hasMessage() && update.getMessage().getChat() != null ? update.getMessage().getChat().getId() : null;
            forwardingService.forward(channel, text, chatId, update.getUpdateId() == null ? null : update.getUpdateId().longValue());
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
