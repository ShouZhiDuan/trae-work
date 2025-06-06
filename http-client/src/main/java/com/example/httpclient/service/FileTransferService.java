package com.example.httpclient.service;

import com.example.httpclient.dto.FileUploadRequest;
import com.example.httpclient.dto.HttpResponse;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 文件传输服务类
 * 提供高性能的文件上传和下载功能
 */
@Service
public class FileTransferService {

    private static final Logger logger = LoggerFactory.getLogger(FileTransferService.class);

    private final OkHttpClient okHttpClient;

    @Autowired
    public FileTransferService(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * 上传文件
     */
    public HttpResponse<String> uploadFile(FileUploadRequest uploadRequest) {
        long startTime = System.currentTimeMillis();
        
        try {
            if (!uploadRequest.hasFile()) {
                return HttpResponse.error(400, "没有找到要上传的文件");
            }

            // 构建多部分请求体
            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

            // 添加文件部分
            addFilePart(bodyBuilder, uploadRequest);

            // 添加额外的表单字段
            uploadRequest.getFormFields().forEach((key, value) -> 
                bodyBuilder.addFormDataPart(key, String.valueOf(value)));

            RequestBody requestBody = bodyBuilder.build();

            // 构建请求
            Request.Builder requestBuilder = new Request.Builder()
                .url(uploadRequest.getUrl())
                .post(requestBody);

            // 添加请求头
            uploadRequest.getHeaders().forEach(requestBuilder::addHeader);

            Request request = requestBuilder.build();

            // 创建自定义超时的客户端
            OkHttpClient clientWithTimeout = okHttpClient.newBuilder()
                .connectTimeout(uploadRequest.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(uploadRequest.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(uploadRequest.getTimeout(), TimeUnit.SECONDS)
                .build();

            // 执行请求
            try (Response response = clientWithTimeout.newCall(request).execute()) {
                return buildUploadResponse(response, startTime);
            }

        } catch (Exception e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            HttpResponse<String> errorResponse = new HttpResponse<>();
            errorResponse.setStatusCode(500);
            errorResponse.setErrorMessage("文件上传失败: " + e.getMessage());
            errorResponse.setSuccess(false);
            errorResponse.setResponseTime(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }

    /**
     * 异步上传文件
     */
    public CompletableFuture<HttpResponse<String>> uploadFileAsync(FileUploadRequest uploadRequest) {
        return CompletableFuture.supplyAsync(() -> uploadFile(uploadRequest));
    }

    /**
     * 下载文件到指定路径
     */
    public HttpResponse<String> downloadFile(String url, String savePath) {
        return downloadFile(url, savePath, null);
    }

    /**
     * 下载文件到指定路径（带请求头）
     */
    public HttpResponse<String> downloadFile(String url, String savePath, 
                                           java.util.Map<String, String> headers) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 构建请求
            Request.Builder requestBuilder = new Request.Builder().url(url);
            
            if (headers != null) {
                headers.forEach(requestBuilder::addHeader);
            }
            
            Request request = requestBuilder.build();

            // 执行请求
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return HttpResponse.error(response.code(), "下载失败: " + response.message());
                }

                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    return HttpResponse.error(500, "响应体为空");
                }

                // 创建目标文件路径
                Path targetPath = Paths.get(savePath);
                Files.createDirectories(targetPath.getParent());

                // 写入文件
                try (InputStream inputStream = responseBody.byteStream();
                     OutputStream outputStream = Files.newOutputStream(targetPath)) {
                    
                    long bytesTransferred = IOUtils.copyLarge(inputStream, outputStream);
                    
                    HttpResponse<String> successResponse = new HttpResponse<>();
                    successResponse.setStatusCode(200);
                    successResponse.setBody("文件下载成功，保存到: " + savePath + "，大小: " + bytesTransferred + " 字节");
                    successResponse.setSuccess(true);
                    successResponse.setResponseTime(System.currentTimeMillis() - startTime);
                    successResponse.setContentLength(bytesTransferred);
                    
                    // 设置响应头
                    response.headers().forEach(pair -> 
                        successResponse.addHeader(pair.getFirst(), pair.getSecond()));
                    
                    return successResponse;
                }

            }

        } catch (Exception e) {
            logger.error("文件下载失败: {}", e.getMessage(), e);
            HttpResponse<String> errorResponse = new HttpResponse<>();
            errorResponse.setStatusCode(500);
            errorResponse.setErrorMessage("文件下载失败: " + e.getMessage());
            errorResponse.setSuccess(false);
            errorResponse.setResponseTime(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }

    /**
     * 异步下载文件
     */
    public CompletableFuture<HttpResponse<String>> downloadFileAsync(String url, String savePath) {
        return CompletableFuture.supplyAsync(() -> downloadFile(url, savePath));
    }

    /**
     * 下载文件到字节数组
     */
    public HttpResponse<byte[]> downloadFileToBytes(String url) {
        return downloadFileToBytes(url, null);
    }

    /**
     * 下载文件到字节数组（带请求头）
     */
    public HttpResponse<byte[]> downloadFileToBytes(String url, 
                                                   java.util.Map<String, String> headers) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 构建请求
            Request.Builder requestBuilder = new Request.Builder().url(url);
            
            if (headers != null) {
                headers.forEach(requestBuilder::addHeader);
            }
            
            Request request = requestBuilder.build();

            // 执行请求
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return HttpResponse.error(response.code(), "下载失败: " + response.message());
                }

                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    return HttpResponse.error(500, "响应体为空");
                }

                byte[] bytes = responseBody.bytes();
                
                HttpResponse<byte[]> successResponse = new HttpResponse<>();
                successResponse.setStatusCode(200);
                successResponse.setBody(bytes);
                successResponse.setSuccess(true);
                successResponse.setResponseTime(System.currentTimeMillis() - startTime);
                successResponse.setContentLength(bytes.length);
                
                // 设置响应头
                response.headers().forEach(pair -> 
                    successResponse.addHeader(pair.getFirst(), pair.getSecond()));
                
                return successResponse;
            }

        } catch (Exception e) {
            logger.error("文件下载到字节数组失败: {}", e.getMessage(), e);
            HttpResponse<byte[]> errorResponse = new HttpResponse<>();
            errorResponse.setStatusCode(500);
            errorResponse.setErrorMessage("文件下载失败: " + e.getMessage());
            errorResponse.setSuccess(false);
            errorResponse.setResponseTime(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }

    /**
     * 添加文件部分到多部分请求体
     */
    private void addFilePart(MultipartBody.Builder bodyBuilder, FileUploadRequest uploadRequest) 
            throws IOException {
        
        String fileName = uploadRequest.getFileName();
        String fieldName = uploadRequest.getFieldName();
        String contentType = uploadRequest.getContentType();
        
        if (!StringUtils.hasText(contentType)) {
            contentType = "application/octet-stream";
        }
        
        MediaType mediaType = MediaType.parse(contentType);
        
        if (uploadRequest.getFile() != null) {
            // Spring MultipartFile
            MultipartFile file = uploadRequest.getFile();
            RequestBody fileBody = RequestBody.create(file.getBytes(), mediaType);
            bodyBuilder.addFormDataPart(fieldName, fileName, fileBody);
            
        } else if (uploadRequest.getLocalFile() != null) {
            // 本地文件
            File localFile = uploadRequest.getLocalFile();
            RequestBody fileBody = RequestBody.create(localFile, mediaType);
            bodyBuilder.addFormDataPart(fieldName, fileName, fileBody);
            
        } else if (uploadRequest.getFileBytes() != null) {
            // 字节数组
            byte[] fileBytes = uploadRequest.getFileBytes();
            RequestBody fileBody = RequestBody.create(fileBytes, mediaType);
            bodyBuilder.addFormDataPart(fieldName, fileName, fileBody);
        }
    }

    /**
     * 构建上传响应对象
     */
    private HttpResponse<String> buildUploadResponse(Response response, long startTime) 
            throws IOException {
        
        HttpResponse<String> httpResponse = new HttpResponse<>();
        httpResponse.setStatusCode(response.code());
        httpResponse.setStatusMessage(response.message());
        httpResponse.setSuccess(response.isSuccessful());
        httpResponse.setResponseTime(System.currentTimeMillis() - startTime);
        
        // 设置响应头
        response.headers().forEach(pair -> 
            httpResponse.addHeader(pair.getFirst(), pair.getSecond()));
        
        // 处理响应体
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String rawBody = responseBody.string();
            httpResponse.setRawBody(rawBody);
            httpResponse.setBody(rawBody);
        }
        
        if (!response.isSuccessful()) {
            httpResponse.setErrorMessage("文件上传失败: " + response.message());
        }
        
        return httpResponse;
    }

    /**
     * 获取文件的MIME类型
     */
    public String getContentType(String fileName) {
        try {
            Path path = Paths.get(fileName);
            String contentType = Files.probeContentType(path);
            return contentType != null ? contentType : "application/octet-stream";
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }
}