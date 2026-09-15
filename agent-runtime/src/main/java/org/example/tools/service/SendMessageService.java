package org.example.tools.service;

import lombok.RequiredArgsConstructor;
import org.example.callback.CallbackResponseClient;
import org.example.tools.config.FileToolsProperties;
import org.example.web.dto.ChannelType;
import org.example.web.dto.FileAttachmentDto;
import org.example.web.dto.FileType;
import org.example.web.dto.GenericResponseDto;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SendMessageService {

    private final CallbackResponseClient callbackResponseClient;
    private final FileToolsProperties fileToolsProperties;

    public void sendFileAttachment(
            String path,
            String caption,
            ChannelType channelType,
            String channelId,
            URI responseUrl
    ) {

        File file = getFile(path);

        FileAttachmentDto attachment = toAttachment(file);

        GenericResponseDto response = new GenericResponseDto(
                channelType,
                channelId,
                caption,
                List.of(attachment)
        );

        callbackResponseClient.sendResponse(responseUrl, response);
    }

    private File getFile(String path) {

        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException(
                    "Dateipfad darf nicht leer sein"
            );
        }

        File file = new File(path);

        if (!file.exists()) {
            throw new IllegalArgumentException(
                    "Datei existiert nicht: " + path
            );
        }

        if (!file.isFile()) {
            throw new IllegalArgumentException(
                    "Pfad ist keine Datei: " + path
            );
        }

        return file;
    }

    private FileAttachmentDto toAttachment(File file) {
        long maxSizeBytes = fileToolsProperties.getMaxSizeBytes();
        if (maxSizeBytes <= 0) {
            throw new IllegalStateException("Configured attachment size limit must be positive");
        }

        if (file.length() > maxSizeBytes) {
            throw new IllegalArgumentException("Attachment exceeds configured limit of " + maxSizeBytes + " bytes");
        }

        byte[] content;
        try {
            content = Files.readAllBytes(file.toPath());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read attachment: " + file.getName(), exception);
        }

        if (content.length > maxSizeBytes) {
            throw new IllegalArgumentException("Attachment exceeds configured limit of " + maxSizeBytes + " bytes");
        }

        return new FileAttachmentDto(file.getName(), determineFileType(file), content);
    }

    private FileType determineFileType(File file) {

        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return FileType.PDF;
        }

        if (fileName.endsWith(".txt")) {
            return FileType.TEXT;
        }

        if (fileName.endsWith(".png")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".gif")
                || fileName.endsWith(".webp")) {

            return FileType.IMAGE;
        }

        return FileType.FILE;
    }
}
