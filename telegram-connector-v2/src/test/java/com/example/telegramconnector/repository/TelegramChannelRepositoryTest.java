package com.example.telegramconnector.repository;

import com.example.telegramconnector.domain.TelegramChannel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TelegramChannelRepositoryTest {

    @Autowired
    private TelegramChannelRepository repository;

    @Test
    void saveAndFindByIdRoundTripsAllFields() {
        String channelId = "channel-test-001";
        String name = "Test Channel";
        String botToken = "123456789:ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefgh";

        TelegramChannel original = new TelegramChannel(channelId, name, botToken);
        repository.save(original);

        var loaded = repository.findById(channelId);

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getChannelId()).isEqualTo(channelId);
        assertThat(loaded.get().getName()).isEqualTo(name);
        assertThat(loaded.get().getBotToken()).isEqualTo(botToken);
    }
}
