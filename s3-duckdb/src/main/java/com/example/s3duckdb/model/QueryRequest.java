package com.example.s3duckdb.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class QueryRequest {
    
    @NotBlank(message = "SQL查询语句不能为空")
    private String sql;
    
    /**
     * CSV文件路径列表（在SeaweedFS中的路径）
     */
    private List<String> csvFiles;
    
    /**
     * 是否包含表头
     */
    private boolean hasHeader = true;
    
    /**
     * CSV分隔符
     */
    private String delimiter = ",";
    
    /**
     * 查询超时时间（秒）
     */
    private int timeoutSeconds = 30;
}