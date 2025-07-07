package com.mmw.metal_micro_wire_backend.dto.overview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 总体统计响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverallStatisticsResponse {
    
    /**
     * 总检测数量
     */
    private Long totalDetectionCount;
    
    /**
     * 本月检测数量
     */
    private Long currentMonthCount;
    
    /**
     * 上月检测数量
     */
    private Long lastMonthCount;
    
    /**
     * 总应用场景数
     */
    private Long totalScenarioCount;
    
    /**
     * 总设备数
     */
    private Long totalDeviceCount;
    
    /**
     * 总合格数量
     */
    private Long totalPassCount;
    
    /**
     * 总不合格数量
     */
    private Long totalFailCount;
    
    /**
     * 总合格率（百分比）
     */
    private Double totalPassRate;
    
    /**
     * 本月合格数量
     */
    private Long currentMonthPassCount;
    
    /**
     * 本月不合格数量
     */
    private Long currentMonthFailCount;
    
    /**
     * 本月合格率（百分比）
     */
    private Double currentMonthPassRate;
}
