package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.*;
import com.mmw.metal_micro_wire_backend.service.TraceabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 质量问题溯源控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/traceability")
@RequiredArgsConstructor
public class TraceabilityController {

    private final TraceabilityService traceabilityService;
    
    /**
     * 执行溯源分析
     */
    @PostMapping("/analysis")
    public BaseResponse<TraceabilityAnalysisResponse> performTraceabilityAnalysis(
            @RequestBody TraceabilityQueryRequest request,
            HttpServletRequest httpRequest) {

        try {
            // 从拦截器设置的属性中获取用户信息（拦截器已验证token）
            String userName = (String) httpRequest.getAttribute("userName");
            Long userId = (Long) httpRequest.getAttribute("userId");

            log.info("执行溯源分析，用户：{}(ID:{}), 维度：{}", userName, userId, request.getDimension());

            return traceabilityService.performTraceabilityAnalysis(request);

        } catch (Exception e) {
            log.error("溯源分析接口异常", e);
            return BaseResponse.error("溯源分析失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取质量统计数据
     */
    @PostMapping("/statistics")
    public BaseResponse<List<QualityStatisticsResponse>> getQualityStatistics(
            @RequestBody TraceabilityQueryRequest request,
            HttpServletRequest httpRequest) {

        try {
            // 从拦截器设置的属性中获取用户信息（拦截器已验证token）
            String userName = (String) httpRequest.getAttribute("userName");
            log.debug("获取质量统计数据，用户：{}", userName);

            return traceabilityService.getQualityStatistics(request);

        } catch (Exception e) {
            log.error("获取质量统计数据接口异常", e);
            return BaseResponse.error("获取质量统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 识别质量问题
     */
    @PostMapping("/issues")
    public BaseResponse<List<QualityIssueResponse>> identifyQualityIssues(
            @RequestBody TraceabilityQueryRequest request,
            HttpServletRequest httpRequest) {

        try {
            // 从拦截器设置的属性中获取用户信息（拦截器已验证token）
            String userName = (String) httpRequest.getAttribute("userName");
            log.debug("识别质量问题，用户：{}", userName);

            return traceabilityService.identifyQualityIssues(request);

        } catch (Exception e) {
            log.error("识别质量问题接口异常", e);
            return BaseResponse.error("识别质量问题失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取问题批次详情
     */
    @GetMapping("/batches/problematic")
    public BaseResponse<List<BatchDetailResponse>> getProblematicBatches(
            @RequestParam String dimension,
            @RequestParam String dimensionValue,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest httpRequest) {

        try {
            // 从拦截器设置的属性中获取用户信息（拦截器已验证token）
            String userName = (String) httpRequest.getAttribute("userName");
            log.debug("获取问题批次详情，用户：{}，维度：{}，值：{}", userName, dimension, dimensionValue);

            return traceabilityService.getProblematicBatches(dimension, dimensionValue, startTime, endTime);

        } catch (Exception e) {
            log.error("获取问题批次详情接口异常", e);
            return BaseResponse.error("获取问题批次详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 发送质量问题通知
     */
    @PostMapping("/notifications/send")
    public BaseResponse<String> sendQualityIssueNotifications(
            @RequestBody List<QualityIssueResponse> qualityIssues,
            HttpServletRequest httpRequest) {

        try {
            // 从拦截器设置的属性中获取用户信息（拦截器已验证token）
            String userName = (String) httpRequest.getAttribute("userName");
            Long userId = (Long) httpRequest.getAttribute("userId");

            log.info("发送质量问题通知，用户：{}(ID:{}), 问题数量：{}", userName, userId, qualityIssues.size());

            return traceabilityService.sendQualityIssueNotifications(qualityIssues);

        } catch (Exception e) {
            log.error("发送质量问题通知接口异常", e);
            return BaseResponse.error("发送质量问题通知失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取生产商质量排名
     */
    @GetMapping("/ranking/manufacturers")
    public BaseResponse<List<QualityStatisticsResponse>> getManufacturerRanking(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String scenarioCode,
            HttpServletRequest httpRequest) {

        try {
            // 从拦截器设置的属性中获取用户信息（拦截器已验证token）
            String userName = (String) httpRequest.getAttribute("userName");
            log.debug("获取生产商质量排名，用户：{}", userName);

            return traceabilityService.getManufacturerRanking(startTime, endTime, scenarioCode);

        } catch (Exception e) {
            log.error("获取生产商质量排名接口异常", e);
            return BaseResponse.error("获取生产商质量排名失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取负责人绩效排名
     */
    @GetMapping("/ranking/responsible-persons")
    public BaseResponse<List<QualityStatisticsResponse>> getResponsiblePersonRanking(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String scenarioCode,
            HttpServletRequest httpRequest) {

        try {
            // 从拦截器设置的属性中获取用户信息（拦截器已验证token）
            String userName = (String) httpRequest.getAttribute("userName");
            log.debug("获取负责人绩效排名，用户：{}", userName);

            return traceabilityService.getResponsiblePersonRanking(startTime, endTime, scenarioCode);

        } catch (Exception e) {
            log.error("获取负责人绩效排名接口异常", e);
            return BaseResponse.error("获取负责人绩效排名失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取工艺类型质量分析
     */
    @GetMapping("/analysis/process-types")
    public BaseResponse<List<QualityStatisticsResponse>> getProcessTypeAnalysis(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String scenarioCode,
            HttpServletRequest httpRequest) {

        try {
            // 从拦截器设置的属性中获取用户信息（拦截器已验证token）
            String userName = (String) httpRequest.getAttribute("userName");
            log.debug("获取工艺类型质量分析，用户：{}", userName);

            return traceabilityService.getProcessTypeAnalysis(startTime, endTime, scenarioCode);

        } catch (Exception e) {
            log.error("获取工艺类型质量分析接口异常", e);
            return BaseResponse.error("获取工艺类型质量分析失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取生产机器质量分析
     */
    @GetMapping("/analysis/production-machines")
    public BaseResponse<List<QualityStatisticsResponse>> getProductionMachineAnalysis(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String scenarioCode,
            HttpServletRequest httpRequest) {

        try {
            // 从拦截器设置的属性中获取用户信息（拦截器已验证token）
            String userName = (String) httpRequest.getAttribute("userName");
            log.debug("获取生产机器质量分析，用户：{}", userName);

            return traceabilityService.getProductionMachineAnalysis(startTime, endTime, scenarioCode);

        } catch (Exception e) {
            log.error("获取生产机器质量分析接口异常", e);
            return BaseResponse.error("获取生产机器质量分析失败：" + e.getMessage());
        }
    }
    
    // ==================== 统计分析接口（不发送邮件） ====================

    /**
     * 全量历史数据质量统计分析
     * 仅用于报表展示，不发送邮件通知
     */
    @PostMapping("/analyze/all")
    public BaseResponse<List<QualityIssueResponse>> analyzeAllQualityIssues(HttpServletRequest httpRequest) {
        try {
            String userName = (String) httpRequest.getAttribute("userName");
            log.info("执行全量历史数据质量统计分析，用户：{}", userName);

            return traceabilityService.analyzeAllQualityIssues();

        } catch (Exception e) {
            log.error("全量历史数据质量统计分析接口异常", e);
            return BaseResponse.error("全量历史数据质量统计分析失败：" + e.getMessage());
        }
    }

    /**
     * 基于时间窗口的质量统计分析
     * 仅用于数据分析，不发送邮件通知
     */
    @PostMapping("/analyze/time-window")
    public BaseResponse<List<QualityIssueResponse>> analyzeQualityIssuesByTimeWindow(
            @RequestBody @Valid TraceabilityQueryRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userName = (String) httpRequest.getAttribute("userName");
            log.info("执行时间窗口质量统计分析，用户：{}，时间范围：{} 至 {}",
                userName, request.getStartTime(), request.getEndTime());

            if (request.getStartTime() == null || request.getEndTime() == null) {
                return BaseResponse.error("开始时间和结束时间不能为空");
            }

            return traceabilityService.analyzeQualityIssuesByTimeWindow(
                request.getStartTime(), request.getEndTime());

        } catch (Exception e) {
            log.error("时间窗口质量统计分析接口异常", e);
            return BaseResponse.error("时间窗口质量统计分析失败：" + e.getMessage());
        }
    }

    // ==================== 自动检测+通知接口 ====================

    /**
     * 自动检测并通知质量问题（使用默认时间窗口）
     * 管理员功能，用于定时任务或手动触发
     */
    @PostMapping("/auto-detect")
    public BaseResponse<String> autoDetectAndNotifyQualityIssues(HttpServletRequest httpRequest) {
        try {
            String userName = (String) httpRequest.getAttribute("userName");
            Long userId = (Long) httpRequest.getAttribute("userId");

            log.info("手动触发自动检测质量问题（默认时间窗口），用户：{}(ID:{})", userName, userId);

            return traceabilityService.autoDetectAndNotifyQualityIssues();

        } catch (Exception e) {
            log.error("自动检测质量问题接口异常", e);
            return BaseResponse.error("自动检测质量问题失败：" + e.getMessage());
        }
    }

    /**
     * 自动检测并通知质量问题（指定时间窗口）
     * 管理员功能，支持自定义时间范围
     */
    @PostMapping("/auto-detect/time-window")
    public BaseResponse<String> autoDetectAndNotifyQualityIssuesWithTimeWindow(
            @RequestBody @Valid TraceabilityQueryRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userName = (String) httpRequest.getAttribute("userName");
            log.info("手动触发自动检测质量问题（指定时间窗口），用户：{}，时间范围：{} 至 {}",
                userName, request.getStartTime(), request.getEndTime());

            if (request.getStartTime() == null || request.getEndTime() == null) {
                return BaseResponse.error("开始时间和结束时间不能为空");
            }

            return traceabilityService.autoDetectAndNotifyQualityIssues(
                request.getStartTime(), request.getEndTime());

        } catch (Exception e) {
            log.error("指定时间窗口自动检测质量问题接口异常", e);
            return BaseResponse.error("指定时间窗口自动检测质量问题失败：" + e.getMessage());
        }
    }

    // ==================== 独立邮件发送接口 ====================

    /**
     * 发送自定义邮件通知
     * 完全独立的邮件发送功能
     */
    @PostMapping("/notifications/send-custom")
    public BaseResponse<String> sendCustomNotification(
            @RequestBody @Valid CustomNotificationRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userName = (String) httpRequest.getAttribute("userName");
            log.info("发送自定义邮件通知，用户：{}，收件人：{}", userName, request.getRecipients());

            return traceabilityService.sendCustomNotification(request);

        } catch (Exception e) {
            log.error("发送自定义邮件通知接口异常", e);
            return BaseResponse.error("发送自定义邮件通知失败：" + e.getMessage());
        }
    }
}
