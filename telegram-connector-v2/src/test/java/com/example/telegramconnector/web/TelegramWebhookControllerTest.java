package com.example.telegramconnector.web;

import com.example.telegramconnector.domain.TelegramChannel;
import com.example.telegramconnector.service.TelegramChannelResolver;
import com.example.telegramconnector.service.TelegramMessageForwardingService;
import com.example.telegramconnector.service.exception.ChannelNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TelegramWebhookController.class)
@Import(GlobalExceptionHandler.class)
class TelegramWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TelegramChannelResolver channelResolver;

    @MockBean
    private TelegramMessageForwardingService forwardingService;

    @Test
    void receiveUpdate_withTextMessage_returnsOkAndForwardsMessage() throws Exception {
        // Given
        String channelId = "test-channel-123";
        TelegramChannel channel = new TelegramChannel(channelId, "Test Channel", "bot-token-123");
        when(channelResolver.resolveChannel(channelId)).thenReturn(channel);

        String updateJson = """
                {
                  "update_id": 1,
                  "message": {
                    "message_id": 1,
                    "date": 1691500000,
                    "chat": { "id": 42, "type": "private" },
                    "text": "Hallo Welt"
                  }
                }
                """;

        // When & Then
        mockMvc.perform(post("/webhook/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());

        verify(forwardingService).forward(eq(channel), eq("Hallo Welt"));
    }

    @Test
    void receiveUpdate_withoutMessage_returnsOkAndDoesNotForward() throws Exception {
        // Given
        String channelId = "test-channel-123";
        TelegramChannel channel = new TelegramChannel(channelId, "Test Channel", "bot-token-123");
        when(channelResolver.resolveChannel(channelId)).thenReturn(channel);

        String updateJson = """
                {
                  "update_id": 2,
                  "edited_message": {
                    "message_id": 2,
                    "date": 1691500000,
                    "chat": { "id": 42, "type": "private" },
                    "text": "Hallo Welt editiert"
                  }
                }
                """;

        // When & Then
        mockMvc.perform(post("/webhook/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());

        verify(forwardingService, never()).forward(any(), any());
    }

    @Test
    void receiveUpdate_withUnknownChannelId_returnsNotFound() throws Exception {
        // Given
        String unknownChannelId = "unknown-channel-456";
        when(channelResolver.resolveChannel(unknownChannelId))
                .thenThrow(new ChannelNotFoundException(unknownChannelId));

        String updateJson = """
                {
                  "update_id": 3,
                  "message": {
                    "message_id": 3,
                    "date": 1691500000,
                    "chat": { "id": 42, "type": "private" },
                    "text": "Hallo Welt"
                  }
                }
                """;

        // When & Then
        mockMvc.perform(post("/webhook/{channelId}", unknownChannelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());

        verify(forwardingService, never()).forward(any(), any());
    }
}
