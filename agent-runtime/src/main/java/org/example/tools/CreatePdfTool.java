package org.example.tools;

import lombok.RequiredArgsConstructor;
import org.example.tools.service.PdfService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
public class CreatePdfTool implements AgentTool{
    @Autowired
    private final PdfService pdfService;
    @Override
    public String getId() {
        return "pdftool";
    }
    @Tool(description="Erstellt eine PDF-Datei und speichert sie.")
    public String createPdf(@ToolParam(description = "Der Textinhalt, der in das PDF-Dokument soll.") String content,
                            @ToolParam(description = "Der Name der PDF-Datei") String fileName){
       File created =  pdfService.createPdf(content, fileName);
       return createPdfResult(created);
    }
    private String createPdfResult(File pdf){
        return "Pdf erstellt unter: " + pdf.getPath();
    }
}
