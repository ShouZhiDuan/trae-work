package com.example.aq2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/**
 * FTP服务模块参数配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FtpConf {
    
    /**
     * FTP服务主机或者IP
     */
    @NotBlank(message = "FTP主机不能为空")
    private String host;
    
    /**
     * FTP端口
     */
    @NotNull(message = "FTP端口不能为空")
    @Min(value = 1, message = "端口号必须大于0")
    @Max(value = 65535, message = "端口号不能超过65535")
    private Integer port;
    
    /**
     * FTP用户名
     */
    @NotBlank(message = "FTP用户名不能为空")
    private String user;
    
    /**
     * FTP密码
     */
    @NotBlank(message = "FTP密码不能为空")
    private String pass;
}