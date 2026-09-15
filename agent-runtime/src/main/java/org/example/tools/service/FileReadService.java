package org.example.tools.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.tools.config.FileToolsProperties;
import org.example.web.dto.IncomingFileDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Locale;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class FileReadService {
    private final FileToolsProperties properties;

    public Path store(String channelId, IncomingFileDto file) {
        Path root = root().resolve(safe(channelId)).normalize();
        Path target = root.resolve(safe(file.fileName())).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("Ungueltiger Dateiname");
        try {
            Files.createDirectories(root);
            if (file.content().length > properties.getMaxSizeBytes()) throw new IllegalArgumentException("Datei ist zu gross");
            Files.write(target, file.content(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return target;
        } catch (IOException e) { throw new IllegalStateException("Datei konnte nicht gespeichert werden", e); }
    }

    public String read(String channelId, String fileName) {
        Path root = root().resolve(safe(channelId)).normalize();
        Path path = root.resolve(safe(fileName)).normalize();
        if (!path.startsWith(root) || !Files.isRegularFile(path)) throw new IllegalArgumentException("Datei nicht gefunden: " + fileName);
        try {
            String lower = path.getFileName().toString().toLowerCase(Locale.ROOT);
            if (lower.endsWith(".pdf")) try (var doc = Loader.loadPDF(Files.readAllBytes(path))) { return new PDFTextStripper().getText(doc); }
            if (lower.endsWith(".docx") || lower.endsWith(".xlsx")) return readZipXml(path);
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) { throw new IllegalStateException("Datei konnte nicht gelesen werden", e); }
    }

    private String readZipXml(Path path) throws IOException {
        StringBuilder out = new StringBuilder();
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(path))) {
            var entry = zip.getNextEntry();
            while (entry != null) {
                if (!entry.isDirectory() && (entry.getName().endsWith("document.xml") || entry.getName().endsWith("sharedStrings.xml") || entry.getName().endsWith("sheet1.xml"))) {
                    out.append(new String(zip.readAllBytes(), StandardCharsets.UTF_8).replaceAll("<[^>]+>", " ")).append('\n');
                }
                entry = zip.getNextEntry();
            }
        }
        return out.toString();
    }

    private Path root() { return Paths.get(properties.getPath()).toAbsolutePath().normalize(); }
    private String safe(String value) { return value.replaceAll("[^a-zA-Z0-9._-]", "_"); }
}
