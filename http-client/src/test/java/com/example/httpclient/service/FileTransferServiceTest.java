package com.example.httpclient.service;

import com.example.httpclient.dto.FileUploadRequest;
import com.example.httpclient.dto.HttpResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件传输服务测试类
 */
@SpringBootTest
class FileTransferServiceTest {

    @Autowired
    private FileTransferService fileTransferService;

    private WireMockServer wireMockServer;
    private String baseUrl;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // 启动WireMock服务器
        wireMockServer = new WireMockServer(8090);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8090);
        baseUrl = "http://localhost:8090";
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testUploadMultipartFile() {
        // 模拟文件上传接口
        stubFor(post(urlEqualTo("/upload"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"File uploaded successfully\", \"fileId\": \"12345\"}")));

        // 创建模拟文件
        String fileContent = "This is a test file content";
        MockMultipartFile mockFile = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            fileContent.getBytes()
        );

        // 创建上传请求
        FileUploadRequest uploadRequest = new FileUploadRequest(baseUrl + "/upload", mockFile);
        uploadRequest.addFormField("description", "Test file upload");

        // 执行上传
        HttpResponse<String> response = fileTransferService.uploadFile(uploadRequest);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getResponseTime() > 0);
    }

    @Test
    void testUploadLocalFile() throws IOException {
        // 模拟文件上传接口
        stubFor(post(urlEqualTo("/upload-local"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Local file uploaded successfully\"}")));

        // 创建临时文件
        Path tempFile = tempDir.resolve("test-local.txt");
        String fileContent = "This is a local test file";
        Files.write(tempFile, fileContent.getBytes());

        // 创建上传请求
        FileUploadRequest uploadRequest = new FileUploadRequest(baseUrl + "/upload-local", tempFile.toFile());
        uploadRequest.addHeader("X-Upload-Source", "local");

        // 执行上传
        HttpResponse<String> response = fileTransferService.uploadFile(uploadRequest);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(201, response.getStatusCode());
    }

    @Test
    void testUploadByteArray() {
        // 模拟文件上传接口
        stubFor(post(urlEqualTo("/upload-bytes"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("Bytes uploaded successfully")));

        // 准备字节数组
        String fileContent = "This is byte array content";
        byte[] fileBytes = fileContent.getBytes();

        // 创建上传请求
        FileUploadRequest uploadRequest = new FileUploadRequest(
            baseUrl + "/upload-bytes", 
            fileBytes, 
            "test-bytes.txt"
        );
        uploadRequest.setContentType("text/plain");

        // 执行上传
        HttpResponse<String> response = fileTransferService.uploadFile(uploadRequest);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testAsyncUpload() throws Exception {
        // 模拟异步文件上传接口
        stubFor(post(urlEqualTo("/upload-async"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("Async upload successful")
                .withFixedDelay(100))); // 模拟延迟

        // 创建模拟文件
        MockMultipartFile mockFile = new MockMultipartFile(
            "file", 
            "async-test.txt", 
            "text/plain", 
            "Async test content".getBytes()
        );

        // 创建上传请求
        FileUploadRequest uploadRequest = new FileUploadRequest(baseUrl + "/upload-async", mockFile);

        // 执行异步上传
        CompletableFuture<HttpResponse<String>> futureResponse = 
            fileTransferService.uploadFileAsync(uploadRequest);

        // 验证结果
        HttpResponse<String> response = futureResponse.get();
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testDownloadFile() throws IOException {
        // 模拟文件下载接口
        String fileContent = "This is downloaded file content";
        stubFor(get(urlEqualTo("/download/test.txt"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withHeader("Content-Length", String.valueOf(fileContent.length()))
                .withBody(fileContent)));

        // 准备下载路径
        Path downloadPath = tempDir.resolve("downloaded-test.txt");

        // 执行下载
        HttpResponse<String> response = fileTransferService.downloadFile(
            baseUrl + "/download/test.txt", 
            downloadPath.toString()
        );

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("下载成功"));
        
        // 验证文件是否存在且内容正确
        assertTrue(Files.exists(downloadPath));
        String downloadedContent = Files.readString(downloadPath);
        assertEquals(fileContent, downloadedContent);
    }

    @Test
    void testDownloadFileWithHeaders() throws IOException {
        // 模拟需要认证的文件下载接口
        String fileContent = "Protected file content";
        stubFor(get(urlEqualTo("/download/protected.txt"))
            .withHeader("Authorization", equalTo("Bearer token123"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody(fileContent)));

        // 准备下载路径和请求头
        Path downloadPath = tempDir.resolve("protected-file.txt");
        java.util.Map<String, String> headers = java.util.Map.of(
            "Authorization", "Bearer token123"
        );

        // 执行下载
        HttpResponse<String> response = fileTransferService.downloadFile(
            baseUrl + "/download/protected.txt", 
            downloadPath.toString(),
            headers
        );

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(200, response.getStatusCode());
        assertTrue(Files.exists(downloadPath));
    }

    @Test
    void testAsyncDownload() throws Exception {
        // 模拟异步文件下载接口
        String fileContent = "Async downloaded content";
        stubFor(get(urlEqualTo("/download/async-test.txt"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody(fileContent)
                .withFixedDelay(100))); // 模拟延迟

        // 准备下载路径
        Path downloadPath = tempDir.resolve("async-downloaded.txt");

        // 执行异步下载
        CompletableFuture<HttpResponse<String>> futureResponse = 
            fileTransferService.downloadFileAsync(
                baseUrl + "/download/async-test.txt", 
                downloadPath.toString()
            );

        // 验证结果
        HttpResponse<String> response = futureResponse.get();
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(200, response.getStatusCode());
        assertTrue(Files.exists(downloadPath));
    }

    @Test
    void testDownloadFileToBytes() {
        // 模拟文件下载接口
        String fileContent = "Binary file content for bytes";
        stubFor(get(urlEqualTo("/download/binary.dat"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/octet-stream")
                .withBody(fileContent)));

        // 执行下载到字节数组
        HttpResponse<byte[]> response = fileTransferService.downloadFileToBytes(
            baseUrl + "/download/binary.dat"
        );

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(fileContent, new String(response.getBody()));
        assertEquals(fileContent.length(), response.getContentLength());
    }

    @Test
    void testUploadError() {
        // 模拟上传失败
        stubFor(post(urlEqualTo("/upload-error"))
            .willReturn(aResponse()
                .withStatus(400)
                .withBody("Bad Request: Invalid file format")));

        // 创建模拟文件
        MockMultipartFile mockFile = new MockMultipartFile(
            "file", 
            "invalid.txt", 
            "text/plain", 
            "invalid content".getBytes()
        );

        // 创建上传请求
        FileUploadRequest uploadRequest = new FileUploadRequest(baseUrl + "/upload-error", mockFile);

        // 执行上传
        HttpResponse<String> response = fileTransferService.uploadFile(uploadRequest);

        // 验证结果
        assertNotNull(response);
        assertFalse(response.isSuccessful());
        assertEquals(400, response.getStatusCode());
        assertTrue(response.isClientError());
    }

    @Test
    void testDownloadError() {
        // 模拟下载失败
        stubFor(get(urlEqualTo("/download/notfound.txt"))
            .willReturn(aResponse()
                .withStatus(404)
                .withBody("File not found")));

        // 准备下载路径
        Path downloadPath = tempDir.resolve("notfound.txt");

        // 执行下载
        HttpResponse<String> response = fileTransferService.downloadFile(
            baseUrl + "/download/notfound.txt", 
            downloadPath.toString()
        );

        // 验证结果
        assertNotNull(response);
        assertFalse(response.isSuccessful());
        assertEquals(404, response.getStatusCode());
        assertFalse(Files.exists(downloadPath));
    }

    @Test
    void testGetContentType() {
        // 测试MIME类型检测
        assertEquals("text/plain", fileTransferService.getContentType("test.txt"));
        assertEquals("application/json", fileTransferService.getContentType("data.json"));
        assertEquals("image/jpeg", fileTransferService.getContentType("photo.jpg"));
        assertEquals("application/pdf", fileTransferService.getContentType("document.pdf"));
        assertEquals("application/octet-stream", fileTransferService.getContentType("unknown.xyz"));
    }

    @Test
    void testUploadWithoutFile() {
        // 测试没有文件的上传请求
        FileUploadRequest uploadRequest = new FileUploadRequest();
        uploadRequest.setUrl(baseUrl + "/upload");

        // 执行上传
        HttpResponse<String> response = fileTransferService.uploadFile(uploadRequest);

        // 验证结果
        assertNotNull(response);
        assertFalse(response.isSuccessful());
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getErrorMessage().contains("没有找到要上传的文件"));
    }
}