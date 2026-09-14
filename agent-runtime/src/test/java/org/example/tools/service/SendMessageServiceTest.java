package org.example.tools.service;

import org.example.api.dto.ChannelType;
import org.example.api.dto.FileType;
import org.example.api.dto.GenericResponseDto;
import org.example.callback.CallbackResponseClient;
import org.example.tools.config.FileToolsProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SendMessageServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void sendsFileContentInsteadOfItsLocalPathToTheRequestCallbackUrl() throws Exception {
        Path document = Files.writeString(temporaryDirectory.resolve("report.pdf"), "test document");
        CallbackResponseClient callbackResponseClient = mock(CallbackResponseClient.class);
        SendMessageService service = new SendMessageService(callbackResponseClient, fileToolsProperties(1024));
        URI responseUrl = URI.create("http://connector:8080/api/v1/responses");

        service.sendFileAttachment(
                document.toString(),
                "Here is the report.",
                ChannelType.TELEGRAM,
                "chat-42",
                responseUrl
        );

        org.mockito.ArgumentCaptor<GenericResponseDto> responseCaptor =
                org.mockito.ArgumentCaptor.forClass(GenericResponseDto.class);
        verify(callbackResponseClient).sendResponse(eq(responseUrl), responseCaptor.capture());

        GenericResponseDto response = responseCaptor.getValue();
        assertThat(response.channelType()).isEqualTo(ChannelType.TELEGRAM);
        assertThat(response.channelId()).isEqualTo("chat-42");
        assertThat(response.content()).isEqualTo("Here is the report.");
        assertThat(response.attachments()).singleElement().satisfies(attachment -> {
            assertThat(attachment.fileName()).isEqualTo("report.pdf");
            assertThat(attachment.type()).isEqualTo(FileType.PDF);
            assertThat(attachment.content()).isEqualTo(Files.readAllBytes(document));
        });
    }

    @Test
    void rejectsAttachmentLargerThanConfiguredLimit() throws Exception {
        Path document = Files.writeString(temporaryDirectory.resolve("large.pdf"), "12345");
        CallbackResponseClient callbackResponseClient = mock(CallbackResponseClient.class);
        SendMessageService service = new SendMessageService(callbackResponseClient, fileToolsProperties(4));

        assertThatThrownBy(() -> service.sendFileAttachment(
                document.toString(),
                "Here is the report.",
                ChannelType.TELEGRAM,
                "chat-42",
                URI.create("http://connector:8080/api/v1/responses")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("configured limit");
    }

    private FileToolsProperties fileToolsProperties(long maxSizeBytes) {
        FileToolsProperties properties = new FileToolsProperties();
        properties.setMaxSizeBytes(maxSizeBytes);
        return properties;
    }
}
