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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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
@PropertySource(value = {"classpath:application.properties"})
public class AttachmentService {

    private final Path root;

    @Value("${store}")
    private String store;

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.endpoint}")
    private String endpoint;

    @Value("${aws.bucket-name}")
    private String bucketName;

    private S3Client s3client;

    public AttachmentService() {
        this.root = Paths.get("upload");
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }

    }

    @PostConstruct
    public void init() {
        if (store.equals("aws")) {
            AwsCredentialsProvider creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
            try {
                s3client = S3Client.builder()
                        .credentialsProvider(creds)
                        .region(Region.of(region))
                        .endpointOverride(new URI(endpoint))
                        .build();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    Attachment store(Attachment attachment, MultipartFile file) {
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
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
        if (store.equals("aws")) {
            File f = new File(path.toString());
            s3client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(attachment.getAttachmentId())
                            .build(),
                    RequestBody.fromFile(f));
            if(!f.delete())
                throw new RuntimeException("Failed to delete file " + filename);
            attachment.setUrl(endpoint + "/" + bucketName + "/" + attachment.getAttachmentId());
        } else {
            attachment.setUrl(path.toUri().toString());
        }
        return attachment;
    }

    public void delete(String filename) {
        if (store.equals("aws")) {
            s3client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build());
        } else {
            try {
                Files.delete(root.resolve(filename));
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
