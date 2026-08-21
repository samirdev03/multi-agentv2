package com.example.telegramconnector.service;

import com.example.telegramconnector.client.TelegramBotClient;
import com.example.telegramconnector.domain.TelegramChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponseDeliveryServiceTest {

    @Mock
    private TelegramChannelResolver channelResolver;

    @Mock
    private TelegramBotClient telegramBotClient;

    @InjectMocks
    private ResponseDeliveryService responseDeliveryService;

    @Test
    void deliver_resolvesChannelAndSendsMessageViaTelegramBotClient() {
        // Given
        String channelId = "test-channel-123";
        TelegramChannel channel = new TelegramChannel(channelId, "Test Channel", "bot-token-123");
        when(channelResolver.resolveChannel(channelId)).thenReturn(channel);
        when(telegramBotClient.sendMessage(eq(channel), eq("Antwort vom Agenten"))).thenReturn(Mono.empty());

        // When
        responseDeliveryService.deliver(channelId, "Antwort vom Agenten");

        // Then
        verify(telegramBotClient).sendMessage(channel, "Antwort vom Agenten");
    }
}
