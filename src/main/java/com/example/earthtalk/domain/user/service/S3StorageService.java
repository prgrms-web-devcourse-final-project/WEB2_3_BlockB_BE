package com.example.earthtalk.domain.user.service;

import com.example.earthtalk.config.S3Config;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.util.UUID;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Config s3Config;

    public S3StorageService(S3Config s3Config) {
        this.s3Config = s3Config;
        this.s3Client = S3Client.builder()
            .region(Region.of(s3Config.getS3().getRegion()))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    s3Config.getCredentials().getAccessKey(),
                    s3Config.getCredentials().getSecretKey()
                )
            ))
            .build();
    }


    public String uploadImage(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(s3Config.getS3().getBucketName())
            .key(fileName)
            .contentType(file.getContentType())
            .acl("public-read")
            .build();

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

        return s3Config.getS3().getUrl() + fileName;
    }
}