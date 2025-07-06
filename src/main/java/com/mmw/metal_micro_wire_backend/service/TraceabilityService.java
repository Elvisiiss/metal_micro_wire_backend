package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 质量问题溯源服务接口
 */
public interface TraceabilityService {
    
    /**
     * 执行溯源分析
     * @param request 溯源查询请求
     * @return 溯源分析结果
     */
    BaseResponse<TraceabilityAnalysisResponse> performTraceabilityAnalysis(TraceabilityQueryRequest request);
    
    /**
     * 获取质量统计数据
     * @param request 溯源查询请求
     * @return 质量统计列表
     */
    BaseResponse<List<QualityStatisticsResponse>> getQualityStatistics(TraceabilityQueryRequest request);
    
    /**
     * 识别质量问题
     * @param request 溯源查询请求
     * @return 质量问题列表
     */
    BaseResponse<List<QualityIssueResponse>> identifyQualityIssues(TraceabilityQueryRequest request);
    
    /**
     * 获取问题批次详情
     * @param dimension 查询维度
     * @param dimensionValue 维度值
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 问题批次详情列表
     */
    BaseResponse<List<BatchDetailResponse>> getProblematicBatches(String dimension, String dimensionValue, 
                                                                String startTime, String endTime);
    
    /**
     * 发送质量问题通知
     * @param qualityIssues 质量问题列表
     * @return 通知发送结果
     */
    BaseResponse<String> sendQualityIssueNotifications(List<QualityIssueResponse> qualityIssues);
    
    /**
     * 获取生产商质量排名
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param scenarioCode 应用场景编号（可选）
     * @return 生产商质量排名
     */
    BaseResponse<List<QualityStatisticsResponse>> getManufacturerRanking(String startTime, String endTime, String scenarioCode);
    
    /**
     * 获取负责人绩效排名
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param scenarioCode 应用场景编号（可选）
     * @return 负责人绩效排名
     */
    BaseResponse<List<QualityStatisticsResponse>> getResponsiblePersonRanking(String startTime, String endTime, String scenarioCode);
    
    /**
     * 获取工艺类型质量分析
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param scenarioCode 应用场景编号（可选）
     * @return 工艺类型质量分析
     */
    BaseResponse<List<QualityStatisticsResponse>> getProcessTypeAnalysis(String startTime, String endTime, String scenarioCode);
    
    /**
     * 获取生产机器质量分析
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param scenarioCode 应用场景编号（可选）
     * @return 生产机器质量分析
     */
    BaseResponse<List<QualityStatisticsResponse>> getProductionMachineAnalysis(String startTime, String endTime, String scenarioCode);
    
    /**
     * 自动检测并通知质量问题（使用默认时间窗口）
     * 定时任务调用，自动检测质量问题并发送通知
     * @return 检测和通知结果
     */
    BaseResponse<String> autoDetectAndNotifyQualityIssues();

    /**
     * 自动检测并通知质量问题（指定时间窗口）
     * @param startTime 检测开始时间
     * @param endTime 检测结束时间
     * @return 检测和通知结果
     */
    BaseResponse<String> autoDetectAndNotifyQualityIssues(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 全量历史数据质量统计分析（仅用于报表展示，不发送邮件）
     * @return 质量问题列表
     */
    BaseResponse<List<QualityIssueResponse>> analyzeAllQualityIssues();

    /**
     * 基于时间窗口的质量统计分析（仅用于数据分析，不发送邮件）
     * @param startTime 分析开始时间
     * @param endTime 分析结束时间
     * @return 质量问题列表
     */
    BaseResponse<List<QualityIssueResponse>> analyzeQualityIssuesByTimeWindow(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 发送自定义邮件通知（完全独立的邮件功能）
     * @param request 自定义邮件请求
     * @return 邮件发送结果
     */
    BaseResponse<String> sendCustomNotification(CustomNotificationRequest request);
}
