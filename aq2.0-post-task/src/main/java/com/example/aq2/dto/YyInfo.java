package com.example.aq2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 医院场景信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YyInfo {
    
    /**
     * 任务清单
     */
    @Valid
    @NotEmpty(message = "任务清单不能为空")
    private List<RunListItem> runList;
}