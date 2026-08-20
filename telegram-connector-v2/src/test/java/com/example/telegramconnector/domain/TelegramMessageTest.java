package com.example.telegramconnector.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelegramMessageTest {

    @Test
    void validValuesCreateRecordWithExpectedAccessors() {
        String message = "Hello, World!";
        String channelId = "channel-123";

        TelegramMessage telegramMessage = new TelegramMessage(message, channelId);

        assertThat(telegramMessage.message()).isEqualTo(message);
        assertThat(telegramMessage.channelId()).isEqualTo(channelId);
    }

    @Test
    void nullMessageThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramMessage(null, "channel-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("message darf nicht leer sein");
    }

    @Test
    void blankMessageThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramMessage("   ", "channel-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("message darf nicht leer sein");
    }

    @Test
    void emptyMessageThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramMessage("", "channel-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("message darf nicht leer sein");
    }

    @Test
    void nullChannelIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramMessage("message", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("channelId darf nicht leer sein");
    }

    @Test
    void blankChannelIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramMessage("message", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("channelId darf nicht leer sein");
    }

    @Test
    void emptyChannelIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new TelegramMessage("message", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("channelId darf nicht leer sein");
    }
}
