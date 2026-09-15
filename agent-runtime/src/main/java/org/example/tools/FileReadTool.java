package org.example.tools;

import lombok.RequiredArgsConstructor;
import org.example.tools.service.FileReadService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileReadTool implements AgentTool {
    private final FileReadService service;
    @Tool(description = "Liest eine vom Benutzer im aktuellen Telegram-Channel gesendete PDF, TXT, Markdown, CSV, JSON, DOCX oder XLSX Datei.")
    public String read(@ToolParam(description = "Exakter Dateiname aus den bereitgestellten Anhängen") String fileName, ToolContext context) {
        String channelId = (String) context.getContext().get("channelId");
        if (channelId == null || channelId.isBlank()) throw new IllegalStateException("channelId fehlt im ToolContext");
        return service.read(channelId, fileName);
    }
    @Override public String getId() { return "fileread"; }
}
