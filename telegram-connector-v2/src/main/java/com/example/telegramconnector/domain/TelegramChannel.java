package com.example.telegramconnector.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.util.Base64;

@Entity
@Table(name = "telegram_channel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // nur fuer JPA, nicht fuer Anwendungscode
public class TelegramChannel {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Id
    @Column(name = "channel_id", nullable = false, updatable = false)
    private String channelId;

    @Column(nullable = false)
    private String name;

    /** Bot-Token von @BotFather. Wird für setWebhook() und künftige ausgehende Bot-API-Aufrufe benötigt. */
    @Column(name = "bot_token", nullable = false)
    private String botToken;

    /** Geheimnis fuer die Webhook-Verifikation (X-Telegram-Bot-Api-Secret-Token). Wird pro Bot generiert. */
    @Column(name = "secret_token")
    private String secretToken;

    public TelegramChannel(String channelId, String name, String botToken) {
        this(channelId, name, botToken, generateSecretToken());
    }

    public TelegramChannel(String channelId, String name, String botToken, String secretToken) {
        this.channelId = requireNonBlank(channelId, "channelId");
        this.name = requireNonBlank(name, "name");
        this.botToken = requireNonBlank(botToken, "botToken");
        this.secretToken = secretToken;
    }

    public static String generateSecretToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " darf nicht leer sein");
        }
        return value;
    }
}