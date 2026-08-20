package com.example.telegramconnector.service;

import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.repository.TelegramChannelRepository;
import com.example.telegramconnector.service.exception.ChannelNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramChannelResolverTest {

    @Mock
    private TelegramChannelRepository channelRepository;

    @InjectMocks
    private TelegramChannelResolver resolver;

    @Test
    void resolveChannel_withKnownChannelId_returnsChannel() {
        // Given
        String channelId = "test-channel-123";
        TelegramChannel expectedChannel = new TelegramChannel(channelId, "Test Channel", "bot-token-123");
        when(channelRepository.findById(channelId)).thenReturn(Optional.of(expectedChannel));

        // When
        TelegramChannel result = resolver.resolveChannel(channelId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getChannelId()).isEqualTo(channelId);
        assertThat(result.getName()).isEqualTo("Test Channel");
    }

    @Test
    void resolveChannel_withUnknownChannelId_throwsChannelNotFoundException() {
        // Given
        String unknownChannelId = "unknown-channel-456";
        when(channelRepository.findById(unknownChannelId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resolver.resolveChannel(unknownChannelId))
                .isInstanceOf(ChannelNotFoundException.class)
                .hasMessageContaining(unknownChannelId)
                .hasMessageContaining("Kein TelegramChannel mit channelId");
    }

    @Test
    void resolveChannel_withUnknownChannelId_exceptionCarriesChannelId() {
        // Given
        String unknownChannelId = "unknown-channel-789";
        when(channelRepository.findById(unknownChannelId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resolver.resolveChannel(unknownChannelId))
                .isInstanceOf(ChannelNotFoundException.class)
                .extracting("channelId")
                .isEqualTo(unknownChannelId);
    }
}
