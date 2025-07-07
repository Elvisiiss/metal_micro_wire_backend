package com.mmw.metal_micro_wire_backend.dto.overview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 应用场景统计响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioStatisticsResponse {
    
    /**
     * 场景统计数据列表
     */
    private List<ScenarioStatistics> scenarioData;
    
    /**
     * 场景统计数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScenarioStatistics {
        
        /**
         * 应用场景编号
         */
        private String scenarioCode;
        
        /**
         * 应用场景名称
         */
        private String scenarioName;
        
        /**
         * 线材类型
         */
        private String wireType;
        
        /**
         * 场景使用次数
         */
        private Long scenarioCount;
    }
}
