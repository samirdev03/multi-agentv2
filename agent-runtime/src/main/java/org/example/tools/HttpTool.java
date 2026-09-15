package org.example.tools;

import lombok.RequiredArgsConstructor;
import org.example.tools.service.HttpContentService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HttpTool implements AgentTool {
    private final HttpContentService service;
    @Tool(description = "Ruft eine oeffentliche HTTP/HTTPS-Webseite oder JSON-API ab und gibt deren Inhalt zur Analyse zurueck.")
    public String fetch(@ToolParam(description = "Oeffentliche HTTP- oder HTTPS-URL") String url) { return service.fetch(url); }
    @Override public String getId() { return "http"; }
}
