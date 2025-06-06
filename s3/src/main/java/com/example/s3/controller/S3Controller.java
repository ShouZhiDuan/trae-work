package com.example.s3.controller;

import com.example.s3.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/s3")
@CrossOrigin(origins = "*")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    /**
     * 上传文件到S3
     * @param file 要上传的文件
     * @param keyName 可选的文件键名
     * @return 上传结果
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "keyName", required = false) String keyName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 上传文件
            String fileUrl = s3Service.uploadFile(file, keyName);
            
            response.put("success", true);
            response.put("message", "文件上传成功");
            response.put("fileUrl", fileUrl);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 从S3下载文件
     * @param keyName S3中的文件键名
     * @return 文件流
     */
    @GetMapping("/download/{keyName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String keyName) {
        try {
            // 检查文件是否存在
            if (!s3Service.fileExists(keyName)) {
                return ResponseEntity.notFound().build();
            }

            // 下载文件
            InputStream inputStream = s3Service.downloadFile(keyName);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + keyName + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除S3中的文件
     * @param keyName S3中的文件键名
     * @return 删除结果
     */
    @DeleteMapping("/delete/{keyName}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String keyName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查文件是否存在
            if (!s3Service.fileExists(keyName)) {
                response.put("success", false);
                response.put("message", "文件不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 删除文件
            boolean deleted = s3Service.deleteFile(keyName);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "文件删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "文件删除失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "文件删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取S3桶中的文件列表
     * @return 文件列表
     */
    @GetMapping("/files")
    public ResponseEntity<Map<String, Object>> listFiles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> files = s3Service.listFiles();
            
            response.put("success", true);
            response.put("message", "获取文件列表成功");
            response.put("files", files);
            response.put("count", files.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取文件列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查文件是否存在
     * @param keyName S3中的文件键名
     * @return 检查结果
     */
    @GetMapping("/exists/{keyName}")
    public ResponseEntity<Map<String, Object>> checkFileExists(@PathVariable String keyName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exists = s3Service.fileExists(keyName);
            
            response.put("success", true);
            response.put("exists", exists);
            response.put("keyName", keyName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检查文件存在性失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}