package com.example.telegramconnector.service;

import com.example.telegramconnector.client.TelegramBotClient;
import com.example.telegramconnector.api.FileAttachmentRequest;
import com.example.telegramconnector.api.FileType;
import com.example.telegramconnector.domain.TelegramChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
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

    @Test
    void deliver_sendsTextThenRoutesImageAndPdfAttachmentsInOrder() {
        // Given
        String channelId = "test-channel-123";
        TelegramChannel channel = new TelegramChannel(channelId, "Test Channel", "bot-token-123");
        FileAttachmentRequest image = new FileAttachmentRequest("chart.png", FileType.IMAGE, new byte[] {1});
        FileAttachmentRequest pdf = new FileAttachmentRequest("answer.pdf", FileType.PDF, new byte[] {2});
        when(channelResolver.resolveChannel(channelId)).thenReturn(channel);
        when(telegramBotClient.sendMessage(channel, "Antwort vom Agenten")).thenReturn(Mono.empty());
        when(telegramBotClient.sendPhoto(channel, image)).thenReturn(Mono.empty());
        when(telegramBotClient.sendDocument(channel, pdf)).thenReturn(Mono.empty());

        // When
        responseDeliveryService.deliver(channelId, "Antwort vom Agenten", java.util.List.of(image, pdf));

        // Then
        var orderedCalls = inOrder(telegramBotClient);
        orderedCalls.verify(telegramBotClient).sendMessage(channel, "Antwort vom Agenten");
        orderedCalls.verify(telegramBotClient).sendPhoto(channel, image);
        orderedCalls.verify(telegramBotClient).sendDocument(channel, pdf);
    }
}
