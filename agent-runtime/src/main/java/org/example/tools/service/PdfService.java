package org.example.tools.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.example.tools.config.FileToolsProperties;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final FileToolsProperties fileToolsProperties;

    public File createPdf(String content, String name) {

        Path directory = Path.of(fileToolsProperties.getPath());

        try {
            // Verzeichnis erstellen, falls es noch nicht existiert
            Files.createDirectories(directory);

            String fileName = name.endsWith(".pdf")
                    ? name
                    : name + ".pdf";

            Path filePath = directory.resolve(fileName);

            try (PDDocument document = new PDDocument()) {

                PDPage page = new PDPage();
                document.addPage(page);

                PDType1Font font = new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );

                try (PDPageContentStream stream =
                             new PDPageContentStream(document, page)) {

                    stream.beginText();
                    stream.setFont(font, 12);
                    stream.setLeading(16);
                    stream.newLineAtOffset(50, 750);

                    for (String line : content.split("\\R")) {
                        stream.showText(line);
                        stream.newLine();
                    }

                    stream.endText();
                }

                document.save(filePath.toFile());
            }

            return filePath.toFile();

        } catch (IOException e) {
            throw new RuntimeException(
                    "PDF konnte nicht erstellt werden: " + name,
                    e
            );
        }
    }
}