package com.example.s3duckdb.controller;

import com.example.s3duckdb.model.ColumnInfo;
import com.example.s3duckdb.model.QueryRequest;
import com.example.s3duckdb.model.QueryResponse;
import com.example.s3duckdb.service.DuckDbQueryService;
import com.example.s3duckdb.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QueryController {

    private final DuckDbQueryService duckDbQueryService;
    private final S3Service s3Service;

    /**
     * 执行SQL查询
     */
    @PostMapping("/execute")
    public ResponseEntity<QueryResponse> executeQuery(@Valid @RequestBody QueryRequest request) {
        log.info("收到查询请求: {}", request.getSql());
        
        try {
            QueryResponse response = duckDbQueryService.executeQuery(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询执行异常: {}", e.getMessage());
            QueryResponse errorResponse = QueryResponse.error("查询执行失败: " + e.getMessage(), request.getSql());
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 获取可用的表列表
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> getAvailableTables() {
        try {
            List<String> tables = duckDbQueryService.getAvailableTables();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tables", tables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取表列表失败: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取表的列信息
     */
    @GetMapping("/tables/{tableName}/columns")
    public ResponseEntity<Map<String, Object>> getTableColumns(@PathVariable String tableName) {
        try {
            List<ColumnInfo> columns = duckDbQueryService.getTableColumns(tableName);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tableName", tableName);
            response.put("columns", columns);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取表列信息失败: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取SeaweedFS中的CSV文件列表
     */
    @GetMapping("/csv-files")
    public ResponseEntity<Map<String, Object>> getCsvFiles() {
        try {
            List<String> csvFiles = s3Service.listCsvFiles();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("csvFiles", csvFiles);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取CSV文件列表失败: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 检查CSV文件是否存在
     */
    @GetMapping("/csv-files/{fileName}/exists")
    public ResponseEntity<Map<String, Object>> checkFileExists(@PathVariable String fileName) {
        try {
            boolean exists = s3Service.fileExists(fileName);
            long fileSize = exists ? s3Service.getFileSize(fileName) : -1;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileName", fileName);
            response.put("exists", exists);
            if (exists) {
                response.put("fileSize", fileSize);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查文件存在性失败: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "S3-DuckDB Query Service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}