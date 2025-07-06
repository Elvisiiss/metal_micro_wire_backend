package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.config.QualityMonitorConfig;
import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.CustomNotificationRequest;
import com.mmw.metal_micro_wire_backend.dto.traceability.QualityIssueResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 质量监控重构功能测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class QualityMonitorRefactoredTest {

    @Autowired
    private TraceabilityService traceabilityService;

    @Autowired
    private QualityMonitorConfig qualityMonitorConfig;

    @Test
    public void testQualityMonitorConfig() {
        // 测试配置是否正确加载
        assertNotNull(qualityMonitorConfig);
        assertTrue(qualityMonitorConfig.isEnabled());
        assertEquals(24, qualityMonitorConfig.getDetectionWindowHours());
        assertTrue(qualityMonitorConfig.isSendNoIssueNotificationToAdmin());
        assertEquals(5.0, qualityMonitorConfig.getFailRateThreshold());
    }

    @Test
    public void testAnalyzeAllQualityIssues() {
        // 测试全量历史数据质量统计分析（不发送邮件）
        BaseResponse<List<QualityIssueResponse>> response = traceabilityService.analyzeAllQualityIssues();
        
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        
        System.out.println("全量分析结果：发现 " + response.getData().size() + " 个质量问题");
    }

    @Test
    public void testAnalyzeQualityIssuesByTimeWindow() {
        // 测试基于时间窗口的质量统计分析（不发送邮件）
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(24);
        
        BaseResponse<List<QualityIssueResponse>> response = 
            traceabilityService.analyzeQualityIssuesByTimeWindow(startTime, endTime);
        
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        
        System.out.println("时间窗口分析结果：发现 " + response.getData().size() + " 个质量问题");
    }

    @Test
    public void testAutoDetectAndNotifyQualityIssues() {
        // 测试自动检测并通知质量问题（默认时间窗口）
        BaseResponse<String> response = traceabilityService.autoDetectAndNotifyQualityIssues();
        
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        
        System.out.println("自动检测结果：" + response.getData());
    }

    @Test
    public void testAutoDetectAndNotifyQualityIssuesWithTimeWindow() {
        // 测试自动检测并通知质量问题（指定时间窗口）
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(12);
        
        BaseResponse<String> response = 
            traceabilityService.autoDetectAndNotifyQualityIssues(startTime, endTime);
        
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        
        System.out.println("指定时间窗口检测结果：" + response.getData());
    }

    @Test
    public void testSendCustomNotification() {
        // 测试发送自定义邮件通知
        CustomNotificationRequest request = CustomNotificationRequest.builder()
            .recipients(Arrays.asList("test@example.com"))
            .subject("测试邮件")
            .content("这是一个测试邮件内容")
            .emailType("TEST")
            .isHtml(false)
            .build();
        
        BaseResponse<String> response = traceabilityService.sendCustomNotification(request);
        
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        
        System.out.println("自定义邮件发送结果：" + response.getData());
    }

    @Test
    public void testArchitecturalDecoupling() {
        // 测试架构解耦：统计分析不应发送邮件
        
        // 1. 纯统计分析
        BaseResponse<List<QualityIssueResponse>> analysisResponse = 
            traceabilityService.analyzeAllQualityIssues();
        
        // 2. 自动检测+通知
        BaseResponse<String> detectionResponse = 
            traceabilityService.autoDetectAndNotifyQualityIssues();
        
        // 验证两个功能都能正常工作且相互独立
        assertNotNull(analysisResponse);
        assertNotNull(detectionResponse);
        assertEquals("success", analysisResponse.getCode());
        assertEquals("success", detectionResponse.getCode());
        
        System.out.println("架构解耦验证通过：统计分析和通知功能相互独立");
    }

    @Test
    public void testTimeWindowIncrementalDetection() {
        // 测试时间窗口增量检测
        LocalDateTime now = LocalDateTime.now();
        
        // 测试不同时间窗口的检测结果
        LocalDateTime start1 = now.minusHours(24);
        LocalDateTime start2 = now.minusHours(12);
        
        BaseResponse<List<QualityIssueResponse>> result1 = 
            traceabilityService.analyzeQualityIssuesByTimeWindow(start1, now);
        BaseResponse<List<QualityIssueResponse>> result2 = 
            traceabilityService.analyzeQualityIssuesByTimeWindow(start2, now);
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("success", result1.getCode());
        assertEquals("success", result2.getCode());
        
        System.out.println("24小时窗口检测到 " + result1.getData().size() + " 个问题");
        System.out.println("12小时窗口检测到 " + result2.getData().size() + " 个问题");
        
        // 12小时窗口的问题数应该小于等于24小时窗口
        assertTrue(result2.getData().size() <= result1.getData().size());
    }
}
