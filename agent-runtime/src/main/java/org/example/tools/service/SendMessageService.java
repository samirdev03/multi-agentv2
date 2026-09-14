package org.example.tools.service;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.*;
import org.example.llm.client.channel.Channel;
import org.example.llm.client.channel.ChannelRegistry;
import org.springframework.stereotype.Service;
import org.example.api.dto.FileType;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SendMessageService {

    private final ChannelRegistry channelRegistry;

    public void sendFileAttachment(
            String path,
            String caption,
            ChannelType channelType,
            String channelId
    ) {

        File file = getFile(path);

        FileAttachmentDto attachment = new FileAttachmentDto(
                file.getAbsolutePath(),
                file.getName(),
                determineFileType(file)
        );

        ResponseDto response = new GenericResponseDto(
                channelType,
                channelId,
                caption,
                List.of(attachment)
        );

        Channel channel = channelRegistry.getChannel(channelType);

        channel.sendResponse(response);
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