package com.example.telegramconnector.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "telegram_channel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // nur fuer JPA, nicht fuer Anwendungscode
public class TelegramChannel {

    @Id
    @Column(name = "channel_id", nullable = false, updatable = false)
    private String channelId;

    @Column(nullable = false)
    private String name;

    /** Bot-Token von @BotFather. Wird für setWebhook() und künftige ausgehende Bot-API-Aufrufe benötigt. */
    @Column(name = "bot_token", nullable = false)
    private String botToken;

    public TelegramChannel(String channelId, String name, String botToken) {
        this.channelId = requireNonBlank(channelId, "channelId");
        this.name = requireNonBlank(name, "name");
        this.botToken = requireNonBlank(botToken, "botToken");
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " darf nicht leer sein");
        }
        return value;
    }
}
