package org.example.tools.service;

import org.example.tools.config.FileToolsProperties;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

class PdfServiceTest {

    @Test
    void createsPdfInNestedConfiguredDirectory() throws Exception {
        var directory = Files.createTempDirectory("pdf-service-test").resolve("agent/ai/files");
        var properties = new FileToolsProperties();
        properties.setPath(directory.toString());

        var file = new PdfService(properties).createPdf("Übersicht – zuverlässig", "bericht");

        assertThat(file).exists().hasName("bericht.pdf");
        assertThat(Files.size(file.toPath())).isPositive();
    }
}
