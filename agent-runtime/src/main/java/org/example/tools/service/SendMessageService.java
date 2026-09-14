package org.example.tools.service;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.*;
import org.example.callback.CallbackResponseClient;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SendMessageService {

    private final CallbackResponseClient callbackResponseClient;

    public void sendFileAttachment(
            String path,
            String caption,
            ChannelType channelType,
            String channelId,
            URI responseUrl
    ) {

        File file = getFile(path);

        FileAttachmentDto attachment = new FileAttachmentDto(
                file.getAbsolutePath(),
                file.getName(),
                determineFileType(file)
        );

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
