package com.mmw.metal_micro_wire_backend.dto.traceability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 溯源分析响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraceabilityAnalysisResponse {
    
    /**
     * 查询维度
     */
    private String dimension;
    
    /**
     * 查询时间范围
     */
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    /**
     * 总体统计信息
     */
    private OverallStatistics overallStatistics;
    
    /**
     * 详细统计列表
     */
    private List<QualityStatisticsResponse> detailStatistics;
    
    /**
     * 问题识别结果
     */
    private List<QualityIssueResponse> qualityIssues;
    
    /**
     * 分页信息
     */
    private PageInfo pageInfo;
    
    /**
     * 总体统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallStatistics {
        /**
         * 总维度数量（如总生产商数量、总负责人数量等）
         */
        private Long totalDimensions;
        
        /**
         * 有问题的维度数量
         */
        private Long problematicDimensions;
        
        /**
         * 总批次数
         */
        private Long totalBatches;
        
        /**
         * 总合格批次数
         */
        private Long totalPassBatches;
        
        /**
         * 总不合格批次数
         */
        private Long totalFailBatches;
        
        /**
         * 整体合格率
         */
        private Double overallPassRate;
        
        /**
         * 整体不合格率
         */
        private Double overallFailRate;
    }
    
    /**
     * 分页信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        /**
         * 当前页码（从0开始）
         */
        private Integer currentPage;
        
        /**
         * 每页大小
         */
        private Integer pageSize;
        
        /**
         * 总页数
         */
        private Integer totalPages;
        
        /**
         * 总记录数
         */
        private Long totalElements;
        
        /**
         * 是否有下一页
         */
        private Boolean hasNext;
        
        /**
         * 是否有上一页
         */
        private Boolean hasPrevious;
    }
}
