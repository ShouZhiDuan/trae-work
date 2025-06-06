package com.example.aq2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.Valid;

/**
 * 查询信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryInfo {
    
    /**
     * 公安场景
     */
    @Valid
    private GaInfo ga;
    
    /**
     * 医院场景
     */
    @Valid
    private YyInfo yy;
}