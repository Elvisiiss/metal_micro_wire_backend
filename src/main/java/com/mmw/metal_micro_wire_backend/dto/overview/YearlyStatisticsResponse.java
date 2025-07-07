package com.mmw.metal_micro_wire_backend.dto.overview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 年度检测数据统计响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearlyStatisticsResponse {
    
    /**
     * 月度统计数据列表
     */
    private List<MonthlyStatistics> monthlyData;
    
    /**
     * 月度统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStatistics {
        
        /**
         * 年份
         */
        private Integer year;
        
        /**
         * 月份
         */
        private Integer month;
        
        /**
         * 合格数量
         */
        private Long passCount;
        
        /**
         * 不合格数量
         */
        private Long failCount;
        
        /**
         * 总数量
         */
        private Long totalCount;
        
        /**
         * 合格率（百分比）
         */
        private Double passRate;
    }
}
