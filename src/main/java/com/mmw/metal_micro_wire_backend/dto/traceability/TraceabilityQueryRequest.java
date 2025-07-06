package com.mmw.metal_micro_wire_backend.dto.traceability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 溯源查询请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraceabilityQueryRequest {
    
    /**
     * 查询维度
     */
    private QueryDimension dimension;
    
    /**
     * 具体查询值（可选，为空时查询所有）
     */
    private String dimensionValue;
    
    /**
     * 开始时间（可选）
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间（可选）
     */
    private LocalDateTime endTime;
    
    /**
     * 应用场景编号（可选）
     */
    private String scenarioCode;
    
    /**
     * 是否只查询有问题的数据
     */
    @Builder.Default
    private Boolean onlyProblematic = false;
    
    /**
     * 不合格率阈值（百分比，用于判断是否有问题）
     */
    @Builder.Default
    private Double failRateThreshold = 5.0;
    
    /**
     * 页码（从0开始）
     */
    @Builder.Default
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    @Builder.Default
    private Integer size = 20;
    
    /**
     * 排序字段
     */
    @Builder.Default
    private String sortBy = "failRate";
    
    /**
     * 排序方向（ASC/DESC）
     */
    @Builder.Default
    private String sortDirection = "DESC";
    
    /**
     * 查询维度枚举
     */
    public enum QueryDimension {
        MANUFACTURER("生产商"),
        RESPONSIBLE_PERSON("负责人"),
        PROCESS_TYPE("工艺类型"),
        PRODUCTION_MACHINE("生产机器");
        
        private final String description;
        
        QueryDimension(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
