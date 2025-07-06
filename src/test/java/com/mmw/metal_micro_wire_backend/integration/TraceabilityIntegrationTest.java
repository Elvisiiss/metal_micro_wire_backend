package com.mmw.metal_micro_wire_backend.integration;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.TraceabilityAnalysisResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.TraceabilityQueryRequest;
import com.mmw.metal_micro_wire_backend.service.TraceabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 溯源分析集成测试
 * 测试ClassCastException修复在实际环境中的效果
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TraceabilityIntegrationTest {

    @Autowired
    private TraceabilityService traceabilityService;

    @Test
    void testPerformTraceabilityAnalysis_ManufacturerDimension() {
        // 准备测试请求
        TraceabilityQueryRequest request = TraceabilityQueryRequest.builder()
                .dimension(TraceabilityQueryRequest.QueryDimension.MANUFACTURER)
                .startTime(LocalDateTime.now().minusDays(30))
                .endTime(LocalDateTime.now())
                .scenarioCode("TEST_SCENARIO")
                .failRateThreshold(5.0)
                .build();

        // 执行溯源分析
        BaseResponse<TraceabilityAnalysisResponse> response = 
                traceabilityService.performTraceabilityAnalysis(request);

        // 验证响应不为空且成功
        assertNotNull(response);
        assertEquals("success", response.getCode());
        
        // 验证数据结构完整性
        if (response.getData() != null) {
            TraceabilityAnalysisResponse data = response.getData();
            assertNotNull(data.getOverallStatistics());
            assertNotNull(data.getDetailStatistics());
            assertNotNull(data.getQualityIssues());
            
            // 验证总体统计数据的合理性
            TraceabilityAnalysisResponse.OverallStatistics overallStats = data.getOverallStatistics();
            assertTrue(overallStats.getTotalBatches() >= 0);
            assertTrue(overallStats.getTotalPassBatches() >= 0);
            assertTrue(overallStats.getTotalFailBatches() >= 0);
            assertTrue(overallStats.getOverallPassRate() >= 0.0);
            assertTrue(overallStats.getOverallFailRate() >= 0.0);
            
            // 验证数据一致性
            assertEquals(overallStats.getTotalBatches(), 
                    overallStats.getTotalPassBatches() + overallStats.getTotalFailBatches());
        }
    }

    @Test
    void testPerformTraceabilityAnalysis_ResponsiblePersonDimension() {
        // 准备测试请求
        TraceabilityQueryRequest request = TraceabilityQueryRequest.builder()
                .dimension(TraceabilityQueryRequest.QueryDimension.RESPONSIBLE_PERSON)
                .startTime(LocalDateTime.now().minusDays(7))
                .endTime(LocalDateTime.now())
                .scenarioCode("TEST_SCENARIO")
                .failRateThreshold(10.0)
                .build();

        // 执行溯源分析
        BaseResponse<TraceabilityAnalysisResponse> response = 
                traceabilityService.performTraceabilityAnalysis(request);

        // 验证响应成功
        assertNotNull(response);
        assertEquals("success", response.getCode());
    }

    @Test
    void testPerformTraceabilityAnalysis_ProcessTypeDimension() {
        // 准备测试请求
        TraceabilityQueryRequest request = TraceabilityQueryRequest.builder()
                .dimension(TraceabilityQueryRequest.QueryDimension.PROCESS_TYPE)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now())
                .failRateThreshold(15.0)
                .build();

        // 执行溯源分析
        BaseResponse<TraceabilityAnalysisResponse> response = 
                traceabilityService.performTraceabilityAnalysis(request);

        // 验证响应成功
        assertNotNull(response);
        assertEquals("success", response.getCode());
    }

    @Test
    void testPerformTraceabilityAnalysis_ProductionMachineDimension() {
        // 准备测试请求
        TraceabilityQueryRequest request = TraceabilityQueryRequest.builder()
                .dimension(TraceabilityQueryRequest.QueryDimension.PRODUCTION_MACHINE)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .failRateThreshold(20.0)
                .build();

        // 执行溯源分析
        BaseResponse<TraceabilityAnalysisResponse> response = 
                traceabilityService.performTraceabilityAnalysis(request);

        // 验证响应成功
        assertNotNull(response);
        assertEquals("success", response.getCode());
    }

    @Test
    void testPerformTraceabilityAnalysis_WithEmptyTimeRange() {
        // 测试空时间范围的情况
        TraceabilityQueryRequest request = TraceabilityQueryRequest.builder()
                .dimension(TraceabilityQueryRequest.QueryDimension.MANUFACTURER)
                .startTime(LocalDateTime.now().plusDays(1)) // 未来时间，应该没有数据
                .endTime(LocalDateTime.now().plusDays(2))
                .scenarioCode("NONEXISTENT_SCENARIO")
                .failRateThreshold(5.0)
                .build();

        // 执行溯源分析
        BaseResponse<TraceabilityAnalysisResponse> response = 
                traceabilityService.performTraceabilityAnalysis(request);

        // 验证响应成功，即使没有数据
        assertNotNull(response);
        assertEquals("success", response.getCode());
        
        if (response.getData() != null) {
            TraceabilityAnalysisResponse.OverallStatistics overallStats = 
                    response.getData().getOverallStatistics();
            assertNotNull(overallStats);
            // 空数据情况下应该返回0值
            assertEquals(0L, overallStats.getTotalBatches());
            assertEquals(0L, overallStats.getTotalPassBatches());
            assertEquals(0L, overallStats.getTotalFailBatches());
            assertEquals(0.0, overallStats.getOverallPassRate());
            assertEquals(0.0, overallStats.getOverallFailRate());
        }
    }
}
