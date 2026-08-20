package com.example.telegramconnector.cli;

import com.example.telegramconnector.client.TelegramBotRegistrationClient;
import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.repository.TelegramChannelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramChannelCliCommandsTest {

    @Mock
    private TelegramChannelRepository channelRepository;

    @Mock
    private TelegramBotRegistrationClient registrationClient;

    @InjectMocks
    private TelegramChannelCliCommands cliCommands;

    @Test
    void addChannel_withNewChannelId_savesChannelRegistersWebhookAndReturnsSuccessMessage() {
        // Given
        String channelId = "test-channel-123";
        String name = "Test Channel";
        String botToken = "bot-token-123";
        when(channelRepository.existsById(channelId)).thenReturn(false);

        // When
        String result = cliCommands.addChannel(channelId, name, botToken);

        // Then
        ArgumentCaptor<TelegramChannel> channelCaptor = ArgumentCaptor.forClass(TelegramChannel.class);
        verify(channelRepository).save(channelCaptor.capture());

        TelegramChannel savedChannel = channelCaptor.getValue();
        assertThat(savedChannel.getChannelId()).isEqualTo(channelId);
        assertThat(savedChannel.getName()).isEqualTo(name);
        assertThat(savedChannel.getBotToken()).isEqualTo(botToken);

        verify(registrationClient).registerWebhook(channelId, botToken);
        assertThat(result).contains("angelegt");
    }

    @Test
    void addChannel_withExistingChannelId_doesNotSaveOrRegisterAndReturnsExistsMessage() {
        // Given
        String channelId = "test-channel-123";
        when(channelRepository.existsById(channelId)).thenReturn(true);

        // When
        String result = cliCommands.addChannel(channelId, "Test Channel", "bot-token-123");

        // Then
        verify(channelRepository, never()).save(any());
        verify(registrationClient, never()).registerWebhook(any(), any());
        assertThat(result).contains("existiert bereits");
    }

    @Test
    void listChannels_withEmptyRepository_returnsEmptyMessage() {
        // Given
        when(channelRepository.findAll()).thenReturn(List.of());

        // When
        String result = cliCommands.listChannels();

        // Then
        assertThat(result).isEqualTo("Keine Channels konfiguriert.");
    }

    @Test
    void listChannels_withPopulatedRepository_returnsChannelIdAndNameForEachChannel() {
        // Given
        TelegramChannel channelOne = new TelegramChannel("channel-one", "Channel One", "token-one");
        TelegramChannel channelTwo = new TelegramChannel("channel-two", "Channel Two", "token-two");
        when(channelRepository.findAll()).thenReturn(List.of(channelOne, channelTwo));

        // When
        String result = cliCommands.listChannels();

        // Then
        assertThat(result).contains("channel-one").contains("Channel One");
        assertThat(result).contains("channel-two").contains("Channel Two");
    }
}
