package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.overview.OverallStatisticsResponse;
import com.mmw.metal_micro_wire_backend.dto.overview.ScenarioStatisticsResponse;
import com.mmw.metal_micro_wire_backend.dto.overview.YearlyStatisticsResponse;
import com.mmw.metal_micro_wire_backend.service.OverViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;

/**
 * 仪表板概览控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/OverView")
@RequiredArgsConstructor
@Validated
public class OverViewController {
    
    private final OverViewService overViewService;
    
    /**
     * 获取最近12个月的年度检测数据统计
     * 权限：已认证用户
     */
    @GetMapping("/year")
    public ResponseEntity<BaseResponse<YearlyStatisticsResponse>> getYearlyStatistics(
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        String userName = (String) httpRequest.getAttribute("userName");
        
        log.info("用户{}({})获取年度检测数据统计", userName, userId);
        
        BaseResponse<YearlyStatisticsResponse> response = overViewService.getYearlyStatistics();
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据时间范围获取应用场景统计
     * 权限：已认证用户
     */
    @GetMapping("/scenario")
    public ResponseEntity<BaseResponse<ScenarioStatisticsResponse>> getScenarioStatistics(
            @RequestParam @Pattern(regexp = "^(this_month|last_month|this_year|last_year|all)$",
                    message = "时间范围参数必须是：this_month、last_month、this_year、last_year、all") String how,
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        String userName = (String) httpRequest.getAttribute("userName");
        
        log.info("用户{}({})获取应用场景统计，时间范围：{}", userName, userId, how);
        
        BaseResponse<ScenarioStatisticsResponse> response = overViewService.getScenarioStatistics(how);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取系统总体统计数据
     * 权限：已认证用户
     */
    @GetMapping("/count")
    public ResponseEntity<BaseResponse<OverallStatisticsResponse>> getOverallStatistics(
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        String userName = (String) httpRequest.getAttribute("userName");
        
        log.info("用户{}({})获取系统总体统计数据", userName, userId);
        
        BaseResponse<OverallStatisticsResponse> response = overViewService.getOverallStatistics();
        return ResponseEntity.ok(response);
    }
}
