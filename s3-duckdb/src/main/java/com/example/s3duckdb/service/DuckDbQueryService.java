package com.example.s3duckdb.service;

import com.example.s3duckdb.model.ColumnInfo;
import com.example.s3duckdb.model.QueryRequest;
import com.example.s3duckdb.model.QueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuckDbQueryService {

    private final JdbcTemplate duckDbJdbcTemplate;
    private final S3Service s3Service;

    @Value("${seaweedfs.bucket}")
    private String bucketName;

    /**
     * 执行查询
     */
    public QueryResponse executeQuery(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 验证和准备CSV文件
            if (request.getCsvFiles() != null && !request.getCsvFiles().isEmpty()) {
                prepareCsvFiles(request.getCsvFiles(), request.isHasHeader(), request.getDelimiter());
            }

            // 2. 执行查询
            List<Map<String, Object>> results = new ArrayList<>();
            List<ColumnInfo> columns = new ArrayList<>();

            duckDbJdbcTemplate.query(request.getSql(), rs -> {
                try {
                    // 获取列信息（只在第一行时获取）
                    if (columns.isEmpty()) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        
                        for (int i = 1; i <= columnCount; i++) {
                            ColumnInfo columnInfo = new ColumnInfo();
                            columnInfo.setName(metaData.getColumnName(i));
                            columnInfo.setType(metaData.getColumnTypeName(i));
                            columnInfo.setNullable(metaData.isNullable(i) == ResultSetMetaData.columnNullable);
                            columnInfo.setDisplaySize(metaData.getColumnDisplaySize(i));
                            columnInfo.setPrecision(metaData.getPrecision(i));
                            columnInfo.setScale(metaData.getScale(i));
                            columns.add(columnInfo);
                        }
                    }

                    // 获取行数据
                    Map<String, Object> row = new HashMap<>();
                    for (ColumnInfo column : columns) {
                        Object value = rs.getObject(column.getName());
                        row.put(column.getName(), value);
                    }
                    results.add(row);
                } catch (SQLException e) {
                    log.error("处理查询结果时发生错误: {}", e.getMessage());
                    throw new RuntimeException("处理查询结果失败", e);
                }
            });

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("查询执行完成，耗时: {}ms，返回行数: {}", executionTime, results.size());

            return QueryResponse.success(results, columns, executionTime, request.getSql());

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("查询执行失败，耗时: {}ms，错误: {}", executionTime, e.getMessage());
            return QueryResponse.error(e.getMessage(), request.getSql());
        }
    }

    /**
     * 准备CSV文件，将S3上的文件注册到DuckDB中
     */
    private void prepareCsvFiles(List<String> csvFiles, boolean hasHeader, String delimiter) {
        for (String csvFile : csvFiles) {
            try {
                // 检查文件是否存在
                if (!s3Service.fileExists(csvFile)) {
                    throw new RuntimeException("CSV文件不存在: " + csvFile);
                }

                // 下载文件到临时目录
                String tempFilePath = downloadCsvToTemp(csvFile);
                
                // 从文件路径生成表名（去掉路径和扩展名）
                String tableName = generateTableName(csvFile);
                
                // 在DuckDB中创建表并导入CSV数据
                createTableFromCsv(tableName, tempFilePath, hasHeader, delimiter);
                
                log.info("成功准备CSV文件: {} -> 表: {}", csvFile, tableName);
                
            } catch (Exception e) {
                log.error("准备CSV文件失败: {}, 错误: {}", csvFile, e.getMessage());
                throw new RuntimeException("准备CSV文件失败: " + csvFile, e);
            }
        }
    }

    /**
     * 下载CSV文件到临时目录
     */
    private String downloadCsvToTemp(String csvFile) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = csvFile.substring(csvFile.lastIndexOf('/') + 1);
        String tempFilePath = tempDir + File.separator + "duckdb_" + System.currentTimeMillis() + "_" + fileName;
        
        try (ResponseInputStream<GetObjectResponse> inputStream = s3Service.getFileInputStream(csvFile);
             FileOutputStream outputStream = new FileOutputStream(tempFilePath)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        
        log.debug("CSV文件已下载到临时路径: {}", tempFilePath);
        return tempFilePath;
    }

    /**
     * 从文件路径生成表名
     */
    private String generateTableName(String csvFile) {
        String fileName = csvFile.substring(csvFile.lastIndexOf('/') + 1);
        if (fileName.toLowerCase().endsWith(".csv")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }
        // 替换特殊字符为下划线
        return fileName.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    /**
     * 在DuckDB中创建表并导入CSV数据
     */
    private void createTableFromCsv(String tableName, String csvFilePath, boolean hasHeader, String delimiter) {
        try {
            // 先删除可能存在的同名表
            duckDbJdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);
            
            // 使用DuckDB的CSV读取功能创建表
            String sql = String.format(
                "CREATE TABLE %s AS SELECT * FROM read_csv_auto('%s', header=%s, delim='%s')",
                tableName, csvFilePath.replace("\\", "/"), hasHeader, delimiter
            );
            
            duckDbJdbcTemplate.execute(sql);
            log.debug("成功创建表: {} 从CSV文件: {}", tableName, csvFilePath);
            
        } catch (Exception e) {
            log.error("创建表失败: {}, 错误: {}", tableName, e.getMessage());
            throw new RuntimeException("创建表失败: " + tableName, e);
        }
    }

    /**
     * 获取所有可用的表
     */
    public List<String> getAvailableTables() {
        try {
            return duckDbJdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'main'",
                String.class
            );
        } catch (Exception e) {
            log.error("获取表列表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取表的列信息
     */
    public List<ColumnInfo> getTableColumns(String tableName) {
        try {
            String sql = "SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_name = ? ORDER BY ordinal_position";
            
            return duckDbJdbcTemplate.query(sql, new Object[]{tableName}, (rs, rowNum) -> {
                ColumnInfo columnInfo = new ColumnInfo();
                columnInfo.setName(rs.getString("column_name"));
                columnInfo.setType(rs.getString("data_type"));
                columnInfo.setNullable("YES".equals(rs.getString("is_nullable")));
                return columnInfo;
            });
        } catch (Exception e) {
            log.error("获取表列信息失败: {}, 错误: {}", tableName, e.getMessage());
            return new ArrayList<>();
        }
    }
}