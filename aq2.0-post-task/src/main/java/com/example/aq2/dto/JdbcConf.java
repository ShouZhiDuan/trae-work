package com.example.aq2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/**
 * 数据库服务模块参数配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JdbcConf {
    
    /**
     * 数据库服务主机或者IP
     */
    @NotBlank(message = "数据库主机不能为空")
    private String host;
    
    /**
     * 数据库端口
     */
    @NotNull(message = "数据库端口不能为空")
    @Min(value = 1, message = "端口号必须大于0")
    @Max(value = 65535, message = "端口号不能超过65535")
    private Integer port;
    
    /**
     * 数据库用户名
     */
    @NotBlank(message = "数据库用户名不能为空")
    private String user;
    
    /**
     * 数据库密码
     */
    @NotBlank(message = "数据库密码不能为空")
    private String pass;
    
    /**
     * 数据库名
     */
    @NotBlank(message = "数据库名不能为空")
    private String dbName;
}