package com.example.earthtalk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "aws")
public class S3Config {
    private Credentials credentials;
    private S3 s3;

    public static class Credentials {
        private String accessKey;
        private String secretKey;

        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    }

    public static class S3 {
        private String bucketName;
        private String url;
        private String region;

        public String getBucketName() { return bucketName; }
        public void setBucketName(String bucketName) { this.bucketName = bucketName; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
    }

    public Credentials getCredentials() { return credentials; }
    public void setCredentials(Credentials credentials) { this.credentials = credentials; }

    public S3 getS3() { return s3; }
    public void setS3(S3 s3) { this.s3 = s3; }
}
