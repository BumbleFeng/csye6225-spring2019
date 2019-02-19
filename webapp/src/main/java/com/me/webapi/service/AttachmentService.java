package com.me.webapi.service;

import com.me.webapi.pojo.Attachment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AttachmentService {

    private final Path root;

    public AttachmentService(){
        this.root = Paths.get("upload");
        try {
            Files.createDirectories(root);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    Attachment store(Attachment attachment, MultipartFile file) {
        System.out.println(file.getOriginalFilename());
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        attachment.setFilename(filename);
        Path path = root.resolve(attachment.getAttachmentId());
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file " + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
        return attachment;
    }

    public void delete(String filename) {
        try {
            Files.delete(root.resolve(filename));
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to delete file " + filename, e);
        }
    }

    Attachment createAttachment(MultipartFile file) {
        Attachment attachment = new Attachment();
        String attachmentId = UUID.randomUUID().toString();
        attachment.setAttachmentId(attachmentId);
        attachment.setUrl(root.resolve(attachmentId).toUri().toString());
        return store(attachment, file);
    }

}
