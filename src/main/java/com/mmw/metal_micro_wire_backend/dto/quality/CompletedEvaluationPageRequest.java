package com.mmw.metal_micro_wire_backend.dto.quality;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 已完成评估线材分页查询请求DTO
 */
@Data
public class CompletedEvaluationPageRequest {
    
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
     * 应用场景编号筛选（可选）
     */
    private String scenarioCode;
    
    /**
     * 排序字段，支持：createTime, modelConfidence
     * 默认按创建时间倒序
     */
    private String sortBy = "createTime";
    
    /**
     * 排序方向，默认降序
     * 对于置信度排序：desc=高置信度优先，asc=低置信度优先
     */
    private String sortDirection = "desc";
} 