package com.example.aq2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

/**
 * 查询请求主体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {
    
    /**
     * 数据库服务模块参数
     */
    @Valid
    @NotNull(message = "数据库配置不能为空")
    private JdbcConf jdbcConf;
    
    /**
     * FTP服务模块参数
     */
    @Valid
    @NotNull(message = "FTP配置不能为空")
    private FtpConf ftpConf;
    
    /**
     * 加密后的查询数据集csv的ftp文件路径
     */
    @NotBlank(message = "查询文件路径不能为空")
    private String queryFilePath;
    
    /**
     * 查询数据集csv中idName
     */
    @NotBlank(message = "查询ID名称不能为空")
    private String queryIdName;
    
    /**
     * 查询信息
     */
    @Valid
    @NotNull(message = "查询信息不能为空")
    private QueryInfo queryInfo;
}