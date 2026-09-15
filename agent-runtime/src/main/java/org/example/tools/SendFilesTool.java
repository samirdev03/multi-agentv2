package org.example.tools;

import lombok.RequiredArgsConstructor;
import org.example.web.dto.ChannelType;
import org.example.tools.service.SendMessageService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class SendFilesTool implements AgentTool {

    private final SendMessageService messageService;

    @Override
    public String getId() {
        return "sendfiles";
    }

    @Tool(description = "Versendet eine PDF-Datei als Antwort an den Benutzer.")
    public String sendPdfAttachment(
            @ToolParam(description = "Pfad zur PDF-Datei, die versendet werden soll")
            String path,

            @ToolParam(description = "Text, der zusammen mit der PDF-Datei gesendet werden soll")
            String caption,

            ToolContext toolContext
    ) {

        String channelId = (String) toolContext
                .getContext()
                .get("channelId");

        ChannelType channelType = (ChannelType) toolContext
                .getContext()
                .get("channelType");

        URI responseUrl = (URI) toolContext
                .getContext()
                .get("responseUrl");

        if (channelId == null || channelId.isBlank()) {
            throw new IllegalStateException(
                    "channelId fehlt im ToolContext"
            );
        }

        if (channelType == null) {
            throw new IllegalStateException(
                    "channelType fehlt im ToolContext"
            );
        }

        if (responseUrl == null) {
            throw new IllegalStateException(
                    "responseUrl fehlt im ToolContext"
            );
        }

        messageService.sendFileAttachment(
                path,
                caption,
                channelType,
                channelId,
                responseUrl
        );

        return "Datei wurde erfolgreich gesendet.";
    }
}
