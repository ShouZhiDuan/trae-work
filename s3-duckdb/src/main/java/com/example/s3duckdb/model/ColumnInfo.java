package com.example.s3duckdb.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnInfo {
    
    /**
     * 列名
     */
    private String name;
    
    /**
     * 列类型
     */
    private String type;
    
    /**
     * 是否可为空
     */
    private boolean nullable;
    
    /**
     * 列的显示大小
     */
    private int displaySize;
    
    /**
     * 列的精度
     */
    private int precision;
    
    /**
     * 列的小数位数
     */
    private int scale;
}