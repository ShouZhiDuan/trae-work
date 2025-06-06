package com.example.s3duckdb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class SeaweedFsConfig {

    @Value("${seaweedfs.endpoint}")
    private String endpoint;

    @Value("${seaweedfs.access-key}")
    private String accessKey;

    @Value("${seaweedfs.secret-key}")
    private String secretKey;

    @Value("${seaweedfs.region:us-east-1}")
    private String region;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .forcePathStyle(true)  // SeaweedFS需要使用路径风格
                .build();
    }
}