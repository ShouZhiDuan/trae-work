package com.example.aq2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 公安场景信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GaInfo {
    
    /**
     * 预警管理需要的参数，只有公安才有这个信息
     */
    @Valid
    @NotNull(message = "公安任务信息不能为空")
    private GaTask task;
    
    /**
     * 任务清单
     */
    @Valid
    @NotEmpty(message = "任务清单不能为空")
    private List<RunListItem> runList;
}