package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.overview.OverallStatisticsResponse;
import com.mmw.metal_micro_wire_backend.dto.overview.ScenarioStatisticsResponse;
import com.mmw.metal_micro_wire_backend.dto.overview.YearlyStatisticsResponse;

/**
 * 仪表板概览服务接口
 */
public interface OverViewService {
    
    /**
     * 获取最近12个月的年度检测数据统计
     * @return 年度统计数据
     */
    BaseResponse<YearlyStatisticsResponse> getYearlyStatistics();
    
    /**
     * 根据时间范围获取应用场景统计
     * @param how 时间范围参数（"这个月"、"上个月"、"今年"、"上一年"、"所有"）
     * @return 应用场景统计数据
     */
    BaseResponse<ScenarioStatisticsResponse> getScenarioStatistics(String how);
    
    /**
     * 获取系统总体统计数据
     * @return 总体统计数据
     */
    BaseResponse<OverallStatisticsResponse> getOverallStatistics();
}
