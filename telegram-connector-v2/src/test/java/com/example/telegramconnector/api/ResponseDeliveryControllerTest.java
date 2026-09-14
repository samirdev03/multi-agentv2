package com.example.telegramconnector.api;

import com.example.telegramconnector.service.ResponseDeliveryService;
import com.example.telegramconnector.service.exception.ChannelNotFoundException;
import com.example.telegramconnector.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResponseDeliveryController.class)
@Import(GlobalExceptionHandler.class)
class ResponseDeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResponseDeliveryService responseDeliveryService;

    @Test
    void deliverResponse_withGenericContentAndPdfAttachment_returnsAcceptedAndDelegatesToService() throws Exception {
        // Given
        String requestJson = """
                {
                  "channelType": "TELEGRAM",
                  "channelId": "test-channel-123",
                  "content": "Antwort vom Agenten",
                  "attachments": [
                    {
                      "path": "C:/files/answer.pdf",
                      "fileName": "answer.pdf",
                      "type": "PDF"
                    }
                  ]
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isAccepted());

        verify(responseDeliveryService).deliver(
                "test-channel-123",
                "Antwort vom Agenten",
                java.util.List.of(new FileAttachmentRequest("C:/files/answer.pdf", "answer.pdf", FileType.PDF)));
    }

    @Test
    void deliverResponse_withUnknownChannelId_returnsNotFound() throws Exception {
        // Given
        String unknownChannelId = "unknown-channel-456";
        doThrow(new ChannelNotFoundException(unknownChannelId))
                .when(responseDeliveryService).deliver(eq(unknownChannelId), eq("Antwort vom Agenten"), anyList());

        String requestJson = """
                {
                  "channelType": "TELEGRAM",
                  "channelId": "%s",
                  "content": "Antwort vom Agenten",
                  "attachments": []
                }
                """.formatted(unknownChannelId);

        // When & Then
        mockMvc.perform(post("/api/v1/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());
    }
}
