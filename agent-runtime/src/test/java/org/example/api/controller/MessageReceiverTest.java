package org.example.api.controller;

import org.example.Service.RequestProcessingService;
import org.example.api.dto.GenericRequestDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageReceiver.class)
class MessageReceiverTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RequestProcessingService processingService;

    @Test
    void receiveMessage_acceptsGenericRequestAndPassesItToProcessing() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "channelType": "TELEGRAM",
                                  "channelId": "12345",
                                  "content": "Hello runtime",
                                  "responseUrl": "http://connector:8080/api/v1/responses"
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<GenericRequestDto> requestCaptor = ArgumentCaptor.forClass(GenericRequestDto.class);
        verify(processingService).process(requestCaptor.capture());

        GenericRequestDto request = requestCaptor.getValue();
        assertThat(request.channelType()).isEqualTo(org.example.api.dto.ChannelType.TELEGRAM);
        assertThat(request.channelId()).isEqualTo("12345");
        assertThat(request.content()).isEqualTo("Hello runtime");
        assertThat(request.responseUrl()).isEqualTo(URI.create("http://connector:8080/api/v1/responses"));
    }
}
