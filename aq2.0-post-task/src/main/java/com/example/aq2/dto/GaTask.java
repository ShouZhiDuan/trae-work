package com.example.aq2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 公安任务信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GaTask {
    
    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long id;
    
    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String name;
    
    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String modelName;
    
    /**
     * 执行用户
     */
    @NotBlank(message = "执行用户不能为空")
    private String user;
    
    /**
     * 查询方机构名称
     */
    @NotBlank(message = "机构名称不能为空")
    private String nodeName;
}