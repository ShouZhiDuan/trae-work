package com.example.s3duckdb.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QueryResponse {
    
    /**
     * 查询是否成功
     */
    private boolean success;
    
    /**
     * 错误信息（如果查询失败）
     */
    private String errorMessage;
    
    /**
     * 查询结果数据
     */
    private List<Map<String, Object>> data;
    
    /**
     * 列信息
     */
    private List<ColumnInfo> columns;
    
    /**
     * 查询执行时间（毫秒）
     */
    private long executionTimeMs;
    
    /**
     * 返回的行数
     */
    private int rowCount;
    
    /**
     * 查询的SQL语句
     */
    private String sql;
    
    public static QueryResponse success(List<Map<String, Object>> data, List<ColumnInfo> columns, long executionTimeMs, String sql) {
        QueryResponse response = new QueryResponse();
        response.setSuccess(true);
        response.setData(data);
        response.setColumns(columns);
        response.setExecutionTimeMs(executionTimeMs);
        response.setRowCount(data != null ? data.size() : 0);
        response.setSql(sql);
        return response;
    }
    
    public static QueryResponse error(String errorMessage, String sql) {
        QueryResponse response = new QueryResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        response.setSql(sql);
        return response;
    }
}