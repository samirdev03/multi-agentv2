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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ResponseDeliveryController.class,
        properties = "telegram-connector.callback-token=test-callback-token")
@Import(GlobalExceptionHandler.class)
class ResponseDeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResponseDeliveryService responseDeliveryService;

    @Test
    void deliverResponse_withMatchingCallbackTokenAndPdfBytes_returnsAcceptedAndDelegatesToService() throws Exception {
        // Given
        String requestJson = """
                {
                  "channelType": "TELEGRAM",
                  "channelId": "test-channel-123",
                  "content": "Antwort vom Agenten",
                  "attachments": [
                    {
                      "fileName": "answer.pdf",
                      "type": "PDF",
                      "content": "cGRmLWNvbnRlbnQ="
                    }
                  ]
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/responses")
                        .header("X-Connector-Token", "test-callback-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isAccepted());

        org.mockito.ArgumentCaptor<java.util.List<FileAttachmentRequest>> attachmentsCaptor =
                org.mockito.ArgumentCaptor.forClass(java.util.List.class);
        verify(responseDeliveryService).deliver(
                eq("test-channel-123"),
                eq("Antwort vom Agenten"),
                attachmentsCaptor.capture());
        assertThat(attachmentsCaptor.getValue()).singleElement().satisfies(attachment -> {
            assertThat(attachment.fileName()).isEqualTo("answer.pdf");
            assertThat(attachment.type()).isEqualTo(FileType.PDF);
            assertThat(attachment.content()).isEqualTo("pdf-content".getBytes());
        });
    }

    @Test
    void deliverResponse_withoutCallbackToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"channelType\":\"TELEGRAM\",\"channelId\":\"test-channel-123\",\"content\":\"Antwort\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(responseDeliveryService);
    }

    @Test
    void deliverResponse_withInvalidCallbackToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/responses")
                        .header("X-Connector-Token", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"channelType\":\"TELEGRAM\",\"channelId\":\"test-channel-123\",\"content\":\"Antwort\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(responseDeliveryService);
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
                        .header("X-Connector-Token", "test-callback-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());
    }
}
