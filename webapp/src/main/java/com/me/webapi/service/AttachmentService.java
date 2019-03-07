package com.me.webapi.service;

import com.me.webapi.pojo.Attachment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AttachmentService {

    @Value("${temp}")
    private String temp;

    @Value("${deployment}")
    private String deployment;

    private Path path;

    @Value("${store}")
    private String store;

    @Value("${aws.region}")
    private String region;

    private String endpoint;

    @Value("${aws.bucket-name}")
    private String bucketName;

    private S3Client s3client;

    public AttachmentService() {
    }

    @PostConstruct
    public void init() {
        this.path = Paths.get(temp);
        if (store.equals("aws")) {
            s3client = S3Client.builder().build();
        }

        if(region.equals("us-east-1"))
            endpoint = "https://s3.amazonaws.com";
        else
            endpoint = "https://s3."+region+".amazonaws.com";

        if(deployment.equals("local")) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Could not initialize storage", e);
            }
        }
    }

    Attachment store(Attachment attachment, MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        attachment.setFilename(filename);
        Path p = path.resolve(attachment.getAttachmentId());
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file " + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, p, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
        if (store.equals("aws")) {
            File f = new File(p.toString());
            s3client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(attachment.getAttachmentId())
                            .build(),
                    RequestBody.fromFile(f));
            if(!f.delete())
                throw new RuntimeException("Failed to delete file " + filename);
            attachment.setUrl(endpoint + "/" + bucketName + "/" + attachment.getAttachmentId());
        } else {
            attachment.setUrl(p.toUri().toString());
        }
        return attachment;
    }

    public void delete(String filename, boolean cloud) {
        if (cloud) {
            s3client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build());
        } else {
            try {
                Files.delete(path.resolve(filename));
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete file " + filename, e);
            }
        }
    }

    Attachment createAttachment(MultipartFile file) {
        Attachment attachment = new Attachment();
        String attachmentId = UUID.randomUUID().toString();
        attachment.setAttachmentId(attachmentId);
        return store(attachment, file);
    }

}
