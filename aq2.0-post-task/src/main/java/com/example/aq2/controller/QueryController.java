package com.example.aq2.controller;

import com.example.aq2.dto.ApiResponse;
import com.example.aq2.dto.QueryRequest;
import com.example.aq2.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 查询请求控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class QueryController {
    
    private final QueryService queryService;
    
    /**
     * 处理查询请求的POST接口
     * 
     * @param request 查询请求参数
     * @return 处理结果
     */
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<Map<String, Object>>> processQuery(
            @Valid @RequestBody QueryRequest request) {
        
        log.info("接收到查询请求，查询文件路径: {}", request.getQueryFilePath());
        
        try {
            // 处理查询请求
            Map<String, Object> result = queryService.processQuery(request);
            
            // 返回成功响应
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("处理查询请求失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "请求处理失败: " + e.getMessage()));
        }
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("服务运行正常"));
    }
    
    /**
     * 获取API信息
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> info() {
        Map<String, Object> info = Map.of(
                "name", "AQ2.0 Post Task API",
                "version", "1.0.0",
                "description", "处理复杂查询请求的Spring Boot API",
                "endpoints", Map.of(
                        "POST /api/v1/query", "处理查询请求",
                        "GET /api/v1/health", "健康检查",
                        "GET /api/v1/info", "获取API信息"
                )
        );
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}