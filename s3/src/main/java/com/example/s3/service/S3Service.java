package com.example.s3.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    /**
     * 上传文件到S3
     * @param file 要上传的文件
     * @param keyName S3中的文件键名
     * @return 上传成功的文件URL
     */
    public String uploadFile(MultipartFile file, String keyName) {
        try {
            // 如果没有提供keyName，则生成一个唯一的文件名
            if (keyName == null || keyName.trim().isEmpty()) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String originalFilename = file.getOriginalFilename();
                keyName = timestamp + "_" + originalFilename;
            }

            // 创建ObjectMetadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, keyName, file.getInputStream(), metadata);
            amazonS3.putObject(putObjectRequest);

            // 返回文件的S3 URL
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, keyName);

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        } catch (AmazonServiceException e) {
            throw new RuntimeException("S3服务异常: " + e.getMessage(), e);
        }
    }

    /**
     * 从S3下载文件
     * @param keyName S3中的文件键名
     * @return 文件的输入流
     */
    public InputStream downloadFile(String keyName) {
        try {
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, keyName);
            S3Object s3Object = amazonS3.getObject(getObjectRequest);
            return s3Object.getObjectContent();

        } catch (AmazonServiceException e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除S3中的文件
     * @param keyName S3中的文件键名
     * @return 删除是否成功
     */
    public boolean deleteFile(String keyName) {
        try {
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, keyName);
            amazonS3.deleteObject(deleteObjectRequest);
            return true;

        } catch (AmazonServiceException e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 列出S3桶中的所有文件
     * @return 文件键名列表
     */
    public List<String> listFiles() {
        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName);
            ObjectListing objectListing = amazonS3.listObjects(listObjectsRequest);

            return objectListing.getObjectSummaries().stream()
                    .map(S3ObjectSummary::getKey)
                    .collect(Collectors.toList());

        } catch (AmazonServiceException e) {
            throw new RuntimeException("获取文件列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件是否存在
     * @param keyName S3中的文件键名
     * @return 文件是否存在
     */
    public boolean fileExists(String keyName) {
        try {
            amazonS3.getObjectMetadata(bucketName, keyName);
            return true;

        } catch (AmazonServiceException e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw new RuntimeException("检查文件存在性失败: " + e.getMessage(), e);
        }
    }
}