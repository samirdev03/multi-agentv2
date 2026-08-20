package com.example.telegramconnector.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelegramChannelTest {

    @Test
    void validValuesCreateObjectWithExpectedGetters() {
        String channelId = "channel-123";
        String name = "My Channel";
        String botToken = "1234567890:ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefgh";

        TelegramChannel channel = new TelegramChannel(channelId, name, botToken);

        assertThat(channel.getChannelId()).isEqualTo(channelId);
        assertThat(channel.getName()).isEqualTo(name);
        assertThat(channel.getBotToken()).isEqualTo(botToken);
    }

    @Test
    void nullChannelIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramChannel(null, "name", "token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("channelId darf nicht leer sein");
    }

    @Test
    void blankChannelIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramChannel("   ", "name", "token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("channelId darf nicht leer sein");
    }

    @Test
    void emptyChannelIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramChannel("", "name", "token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("channelId darf nicht leer sein");
    }

    @Test
    void nullNameThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramChannel("id", null, "token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name darf nicht leer sein");
    }

    @Test
    void blankNameThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramChannel("id", "   ", "token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name darf nicht leer sein");
    }

    @Test
    void emptyNameThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramChannel("id", "", "token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name darf nicht leer sein");
    }

    @Test
    void nullBotTokenThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramChannel("id", "name", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("botToken darf nicht leer sein");
    }

    @Test
    void blankBotTokenThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramChannel("id", "name", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("botToken darf nicht leer sein");
    }

    @Test
    void emptyBotTokenThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramChannel("id", "name", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("botToken darf nicht leer sein");
    }
}
