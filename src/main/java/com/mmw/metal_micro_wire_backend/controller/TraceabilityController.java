package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.*;
import com.mmw.metal_micro_wire_backend.service.TraceabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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
    
    /**
     * 自动检测并通知质量问题
     * 管理员功能，用于定时任务或手动触发
     */
    @PostMapping("/auto-detect")
    public BaseResponse<String> autoDetectAndNotifyQualityIssues(HttpServletRequest httpRequest) {

        try {
            // 从拦截器设置的属性中获取用户信息（拦截器已验证token）
            String userName = (String) httpRequest.getAttribute("userName");
            Long userId = (Long) httpRequest.getAttribute("userId");

            log.info("手动触发自动检测质量问题，用户：{}(ID:{})", userName, userId);

            return traceabilityService.autoDetectAndNotifyQualityIssues();

        } catch (Exception e) {
            log.error("自动检测质量问题接口异常", e);
            return BaseResponse.error("自动检测质量问题失败：" + e.getMessage());
        }
    }
}
