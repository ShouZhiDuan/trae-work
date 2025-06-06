package com.example.httpclient.controller;

import com.example.httpclient.dto.FileUploadRequest;
import com.example.httpclient.dto.HttpRequest;
import com.example.httpclient.dto.HttpResponse;
import com.example.httpclient.service.FileTransferService;
import com.example.httpclient.service.HttpClientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP客户端控制器
 * 提供RESTful API接口
 */
@RestController
@RequestMapping("/api/http-client")
@CrossOrigin(origins = "*")
public class HttpClientController {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientController.class);

    private final HttpClientService httpClientService;
    private final FileTransferService fileTransferService;

    @Autowired
    public HttpClientController(HttpClientService httpClientService, 
                               FileTransferService fileTransferService) {
        this.httpClientService = httpClientService;
        this.fileTransferService = fileTransferService;
    }

    /**
     * 执行HTTP请求
     */
    @PostMapping("/request")
    public ResponseEntity<HttpResponse<Object>> executeRequest(@Valid @RequestBody HttpRequest request) {
        logger.info("执行HTTP请求: {}", request);
        
        try {
            HttpResponse<Object> response = httpClientService.execute(request, Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("HTTP请求执行失败", e);
            return ResponseEntity.internalServerError()
                .body(HttpResponse.error(500, "请求执行失败: " + e.getMessage()));
        }
    }

    /**
     * 执行异步HTTP请求
     */
    @PostMapping("/request/async")
    public CompletableFuture<ResponseEntity<HttpResponse<Object>>> executeRequestAsync(
            @Valid @RequestBody HttpRequest request) {
        logger.info("执行异步HTTP请求: {}", request);
        
        return httpClientService.executeAsync(request, Object.class)
            .thenApply(ResponseEntity::ok)
            .exceptionally(throwable -> {
                logger.error("异步HTTP请求执行失败", throwable);
                return ResponseEntity.internalServerError()
                    .body(HttpResponse.error(500, "异步请求执行失败: " + throwable.getMessage()));
            });
    }

    /**
     * 执行响应式HTTP请求
     */
    @PostMapping("/request/reactive")
    public Mono<ResponseEntity<HttpResponse<Object>>> executeRequestReactive(
            @Valid @RequestBody HttpRequest request) {
        logger.info("执行响应式HTTP请求: {}", request);
        
        return httpClientService.executeReactive(request, Object.class)
            .map(ResponseEntity::ok)
            .onErrorResume(throwable -> {
                logger.error("响应式HTTP请求执行失败", throwable);
                return Mono.just(ResponseEntity.internalServerError()
                    .body(HttpResponse.error(500, "响应式请求执行失败: " + throwable.getMessage())));
            });
    }

    /**
     * GET请求
     */
    @GetMapping("/get")
    public ResponseEntity<HttpResponse<Object>> get(
            @RequestParam String url,
            @RequestParam(required = false) Map<String, String> headers) {
        logger.info("执行GET请求: {}", url);
        
        try {
            HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.GET);
            if (headers != null) {
                request.setHeaders(headers);
            }
            
            HttpResponse<Object> response = httpClientService.execute(request, Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("GET请求执行失败", e);
            return ResponseEntity.internalServerError()
                .body(HttpResponse.error(500, "GET请求执行失败: " + e.getMessage()));
        }
    }

    /**
     * POST请求
     */
    @PostMapping("/post")
    public ResponseEntity<HttpResponse<Object>> post(
            @RequestParam String url,
            @RequestBody(required = false) Object body,
            @RequestParam(required = false) Map<String, String> headers) {
        logger.info("执行POST请求: {}", url);
        
        try {
            HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.POST);
            if (body != null) {
                request.setJsonBody(body);
            }
            if (headers != null) {
                request.setHeaders(headers);
            }
            
            HttpResponse<Object> response = httpClientService.execute(request, Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("POST请求执行失败", e);
            return ResponseEntity.internalServerError()
                .body(HttpResponse.error(500, "POST请求执行失败: " + e.getMessage()));
        }
    }

    /**
     * PUT请求
     */
    @PutMapping("/put")
    public ResponseEntity<HttpResponse<Object>> put(
            @RequestParam String url,
            @RequestBody(required = false) Object body,
            @RequestParam(required = false) Map<String, String> headers) {
        logger.info("执行PUT请求: {}", url);
        
        try {
            HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.PUT);
            if (body != null) {
                request.setJsonBody(body);
            }
            if (headers != null) {
                request.setHeaders(headers);
            }
            
            HttpResponse<Object> response = httpClientService.execute(request, Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("PUT请求执行失败", e);
            return ResponseEntity.internalServerError()
                .body(HttpResponse.error(500, "PUT请求执行失败: " + e.getMessage()));
        }
    }

    /**
     * DELETE请求
     */
    @DeleteMapping("/delete")
    public ResponseEntity<HttpResponse<Object>> delete(
            @RequestParam String url,
            @RequestParam(required = false) Map<String, String> headers) {
        logger.info("执行DELETE请求: {}", url);
        
        try {
            HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.DELETE);
            if (headers != null) {
                request.setHeaders(headers);
            }
            
            HttpResponse<Object> response = httpClientService.execute(request, Object.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("DELETE请求执行失败", e);
            return ResponseEntity.internalServerError()
                .body(HttpResponse.error(500, "DELETE请求执行失败: " + e.getMessage()));
        }
    }

    /**
     * 上传文件
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HttpResponse<String>> uploadFile(
            @RequestParam String url,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Map<String, String> headers,
            @RequestParam(required = false) Map<String, Object> formFields) {
        logger.info("上传文件到: {}, 文件名: {}, 大小: {} 字节", url, file.getOriginalFilename(), file.getSize());
        
        try {
            FileUploadRequest uploadRequest = new FileUploadRequest(url, file);
            
            if (headers != null) {
                uploadRequest.setHeaders(headers);
            }
            
            if (formFields != null) {
                uploadRequest.setFormFields(formFields);
            }
            
            HttpResponse<String> response = fileTransferService.uploadFile(uploadRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            return ResponseEntity.internalServerError()
                .body(HttpResponse.error(500, "文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 异步上传文件
     */
    @PostMapping(value = "/upload/async", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<HttpResponse<String>>> uploadFileAsync(
            @RequestParam String url,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Map<String, String> headers,
            @RequestParam(required = false) Map<String, Object> formFields) {
        logger.info("异步上传文件到: {}, 文件名: {}, 大小: {} 字节", url, file.getOriginalFilename(), file.getSize());
        
        FileUploadRequest uploadRequest = new FileUploadRequest(url, file);
        
        if (headers != null) {
            uploadRequest.setHeaders(headers);
        }
        
        if (formFields != null) {
            uploadRequest.setFormFields(formFields);
        }
        
        return fileTransferService.uploadFileAsync(uploadRequest)
            .thenApply(ResponseEntity::ok)
            .exceptionally(throwable -> {
                logger.error("异步文件上传失败", throwable);
                return ResponseEntity.internalServerError()
                    .body(HttpResponse.error(500, "异步文件上传失败: " + throwable.getMessage()));
            });
    }

    /**
     * 下载文件
     */
    @GetMapping("/download")
    public ResponseEntity<HttpResponse<String>> downloadFile(
            @RequestParam String url,
            @RequestParam String savePath,
            @RequestParam(required = false) Map<String, String> headers) {
        logger.info("下载文件从: {} 到: {}", url, savePath);
        
        try {
            HttpResponse<String> response = fileTransferService.downloadFile(url, savePath, headers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("文件下载失败", e);
            return ResponseEntity.internalServerError()
                .body(HttpResponse.error(500, "文件下载失败: " + e.getMessage()));
        }
    }

    /**
     * 异步下载文件
     */
    @GetMapping("/download/async")
    public CompletableFuture<ResponseEntity<HttpResponse<String>>> downloadFileAsync(
            @RequestParam String url,
            @RequestParam String savePath,
            @RequestParam(required = false) Map<String, String> headers) {
        logger.info("异步下载文件从: {} 到: {}", url, savePath);
        
        return fileTransferService.downloadFileAsync(url, savePath)
            .thenApply(ResponseEntity::ok)
            .exceptionally(throwable -> {
                logger.error("异步文件下载失败", throwable);
                return ResponseEntity.internalServerError()
                    .body(HttpResponse.error(500, "异步文件下载失败: " + throwable.getMessage()));
            });
    }

    /**
     * 下载文件到字节数组
     */
    @GetMapping("/download/bytes")
    public ResponseEntity<HttpResponse<byte[]>> downloadFileToBytes(
            @RequestParam String url,
            @RequestParam(required = false) Map<String, String> headers) {
        logger.info("下载文件到字节数组: {}", url);
        
        try {
            HttpResponse<byte[]> response = fileTransferService.downloadFileToBytes(url, headers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("文件下载到字节数组失败", e);
            return ResponseEntity.internalServerError()
                .body(HttpResponse.error(500, "文件下载失败: " + e.getMessage()));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "HTTP Client Service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}