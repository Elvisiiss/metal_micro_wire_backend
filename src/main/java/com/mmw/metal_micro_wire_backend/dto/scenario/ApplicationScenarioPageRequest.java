package com.mmw.metal_micro_wire_backend.dto.scenario;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 应用场景分页查询请求DTO
 */
@Data
public class ApplicationScenarioPageRequest {
    
    /**
     * 页码，从0开始
     */
    @Min(value = 0, message = "页码不能小于0")
    private int page = 0;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private int size = 10;
    
    /**
     * 线材类型筛选（可选）
     */
    @Pattern(regexp = "^(Cu|Al|Ni|Ti|Zn)?$", message = "线材类型必须是Cu、Al、Ni、Ti、Zn中的一种或为空")
    private String wireType;
    
    /**
     * 应用场景名称关键词搜索（可选）
     */
    private String scenarioNameKeyword;
    
    /**
     * 排序字段，默认按创建时间倒序
     */
    private String sortBy = "createTime";
    
    /**
     * 排序方向，默认降序
     */
    private String sortDirection = "desc";
} 