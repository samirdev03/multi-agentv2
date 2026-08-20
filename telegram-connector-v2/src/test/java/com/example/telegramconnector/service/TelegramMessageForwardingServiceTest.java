package com.example.telegramconnector.service;

import com.example.telegramconnector.client.AgentRuntimeClient;
import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.domain.TelegramMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramMessageForwardingServiceTest {

    @Mock
    private AgentRuntimeClient agentRuntimeClient;

    @InjectMocks
    private TelegramMessageForwardingService forwardingService;

    @Test
    void forward_composesTelegramMessageFromChannelAndRawTextAndSendsIt() {
        // Given
        TelegramChannel channel = new TelegramChannel("test-channel-123", "Test Channel", "bot-token-123");
        String rawText = "Hallo Welt";
        when(agentRuntimeClient.sendAsync(any())).thenReturn(Mono.empty());

        // When
        forwardingService.forward(channel, rawText);

        // Then
        ArgumentCaptor<TelegramMessage> messageCaptor = ArgumentCaptor.forClass(TelegramMessage.class);
        verify(agentRuntimeClient).sendAsync(messageCaptor.capture());

        TelegramMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.message()).isEqualTo(rawText);
        assertThat(sentMessage.channelId()).isEqualTo(channel.getChannelId());
    }
}
