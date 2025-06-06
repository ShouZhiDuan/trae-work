package com.example.aq2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 任务运行列表项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunListItem {
    
    /**
     * 当前查询的表名
     */
    @NotBlank(message = "表名不能为空")
    private String tbName;
    
    /**
     * 当前表名检索的列名
     */
    @NotBlank(message = "列名不能为空")
    private String idName;
    
    /**
     * 公钥
     */
    @NotBlank(message = "公钥不能为空")
    private String publicKey;
    
    /**
     * 响应文件路径
     */
    @NotBlank(message = "响应文件路径不能为空")
    private String responseFilePath;
}