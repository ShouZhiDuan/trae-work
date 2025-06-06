package com.example.s3duckdb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${seaweedfs.bucket}")
    private String bucketName;

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("检查文件存在性时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取文件内容作为字符串
     */
    public String getFileContent(String key) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            log.error("读取文件内容时发生错误: {}", e.getMessage());
            throw new IOException("无法读取文件: " + key, e);
        }
    }

    /**
     * 获取文件的输入流
     */
    public ResponseInputStream<GetObjectResponse> getFileInputStream(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            log.error("获取文件输入流时发生错误: {}", e.getMessage());
            throw new RuntimeException("无法获取文件输入流: " + key, e);
        }
    }

    /**
     * 列出bucket中的所有CSV文件
     */
    public List<String> listCsvFiles() {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);
            
            return response.contents().stream()
                    .map(S3Object::key)
                    .filter(key -> key.toLowerCase().endsWith(".csv"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("列出CSV文件时发生错误: {}", e.getMessage());
            throw new RuntimeException("无法列出CSV文件", e);
        }
    }

    /**
     * 获取文件大小
     */
    public long getFileSize(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headRequest);
            return response.contentLength();
        } catch (Exception e) {
            log.error("获取文件大小时发生错误: {}", e.getMessage());
            return -1;
        }
    }
}