package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.TraceabilityAnalysisResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.TraceabilityQueryRequest;
import com.mmw.metal_micro_wire_backend.repository.WireMaterialRepository;
import com.mmw.metal_micro_wire_backend.service.impl.TraceabilityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 溯源分析服务测试类
 * 主要测试ClassCastException修复
 */
@ExtendWith(MockitoExtension.class)
class TraceabilityServiceTest {

    @Mock
    private WireMaterialRepository wireMaterialRepository;

    @InjectMocks
    private TraceabilityServiceImpl traceabilityService;

    private TraceabilityQueryRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = TraceabilityQueryRequest.builder()
                .dimension(TraceabilityQueryRequest.QueryDimension.MANUFACTURER)
                .startTime(LocalDateTime.now().minusDays(30))
                .endTime(LocalDateTime.now())
                .scenarioCode("TEST_SCENARIO")
                .failRateThreshold(5.0)
                .build();
    }

    @Test
    void testCalculateOverallStatistics_WithValidData() {
        // 准备测试数据 - 模拟正常的查询结果
        List<Object[]> overallStatistics = new ArrayList<>();
        Object[] statisticsRow = {100L, 80L, 20L}; // totalCount, passCount, failCount
        overallStatistics.add(statisticsRow);

        // 模拟维度统计查询结果
        List<Object[]> dimensionStatistics = new ArrayList<>();
        Object[] dimensionRow = {"厂商A", 50L, 40L, 10L, 0L, 0L, "test@example.com"};
        dimensionStatistics.add(dimensionRow);

        // 设置Mock行为
        when(wireMaterialRepository.getOverallStatistics(any(), any(), any()))
                .thenReturn(overallStatistics);
        when(wireMaterialRepository.getManufacturerStatistics(any(), any(), any(), any()))
                .thenReturn(dimensionStatistics);

        // 执行测试
        BaseResponse<TraceabilityAnalysisResponse> response =
                traceabilityService.performTraceabilityAnalysis(testRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        
        TraceabilityAnalysisResponse.OverallStatistics overallStats = 
                response.getData().getOverallStatistics();
        assertNotNull(overallStats);
        assertEquals(100L, overallStats.getTotalBatches());
        assertEquals(80L, overallStats.getTotalPassBatches());
        assertEquals(20L, overallStats.getTotalFailBatches());
        assertEquals(80.0, overallStats.getOverallPassRate(), 0.01);
        assertEquals(20.0, overallStats.getOverallFailRate(), 0.01);
    }

    @Test
    void testCalculateOverallStatistics_WithEmptyResult() {
        // 准备测试数据 - 模拟空查询结果
        List<Object[]> emptyStatistics = Collections.emptyList();

        // 设置Mock行为
        when(wireMaterialRepository.getOverallStatistics(any(), any(), any()))
                .thenReturn(emptyStatistics);
        when(wireMaterialRepository.getManufacturerStatistics(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // 执行测试
        BaseResponse<TraceabilityAnalysisResponse> response =
                traceabilityService.performTraceabilityAnalysis(testRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        
        TraceabilityAnalysisResponse.OverallStatistics overallStats = 
                response.getData().getOverallStatistics();
        assertNotNull(overallStats);
        assertEquals(0L, overallStats.getTotalBatches());
        assertEquals(0L, overallStats.getTotalPassBatches());
        assertEquals(0L, overallStats.getTotalFailBatches());
        assertEquals(0.0, overallStats.getOverallPassRate());
        assertEquals(0.0, overallStats.getOverallFailRate());
    }

    @Test
    void testCalculateOverallStatistics_WithNullValues() {
        // 准备测试数据 - 模拟包含null值的查询结果
        List<Object[]> statisticsWithNulls = new ArrayList<>();
        Object[] statisticsRow = {null, 50L, null}; // 包含null值
        statisticsWithNulls.add(statisticsRow);

        // 设置Mock行为
        when(wireMaterialRepository.getOverallStatistics(any(), any(), any()))
                .thenReturn(statisticsWithNulls);
        when(wireMaterialRepository.getManufacturerStatistics(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // 执行测试
        BaseResponse<TraceabilityAnalysisResponse> response =
                traceabilityService.performTraceabilityAnalysis(testRequest);

        // 验证结果 - 应该能正常处理null值
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        
        TraceabilityAnalysisResponse.OverallStatistics overallStats = 
                response.getData().getOverallStatistics();
        assertNotNull(overallStats);
        // null值应该被转换为0
        assertEquals(0L, overallStats.getTotalBatches());
        assertEquals(50L, overallStats.getTotalPassBatches());
        assertEquals(0L, overallStats.getTotalFailBatches());
    }

    @Test
    void testCalculateOverallStatistics_WithStringNumbers() {
        // 准备测试数据 - 模拟字符串类型的数字
        List<Object[]> statisticsWithStrings = new ArrayList<>();
        Object[] statisticsRow = {"100", "80", "20"}; // 字符串类型的数字
        statisticsWithStrings.add(statisticsRow);

        // 设置Mock行为
        when(wireMaterialRepository.getOverallStatistics(any(), any(), any()))
                .thenReturn(statisticsWithStrings);
        when(wireMaterialRepository.getManufacturerStatistics(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // 执行测试
        BaseResponse<TraceabilityAnalysisResponse> response =
                traceabilityService.performTraceabilityAnalysis(testRequest);

        // 验证结果 - 应该能正常处理字符串数字
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        
        TraceabilityAnalysisResponse.OverallStatistics overallStats = 
                response.getData().getOverallStatistics();
        assertNotNull(overallStats);
        assertEquals(100L, overallStats.getTotalBatches());
        assertEquals(80L, overallStats.getTotalPassBatches());
        assertEquals(20L, overallStats.getTotalFailBatches());
    }
}
