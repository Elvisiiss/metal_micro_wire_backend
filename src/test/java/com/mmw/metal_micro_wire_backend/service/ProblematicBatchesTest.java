package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.BatchDetailResponse;
import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import com.mmw.metal_micro_wire_backend.repository.WireMaterialRepository;
import com.mmw.metal_micro_wire_backend.service.impl.TraceabilityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 问题批次查询功能测试
 * 测试日期时间解析和数据库查询修复效果
 */
@ExtendWith(MockitoExtension.class)
class ProblematicBatchesTest {

    @Mock
    private WireMaterialRepository wireMaterialRepository;

    @InjectMocks
    private TraceabilityServiceImpl traceabilityService;

    private WireMaterial testWireMaterial;

    @BeforeEach
    void setUp() {
        testWireMaterial = WireMaterial.builder()
                .batchNumber("TEST_BATCH_001")
                .manufacturer("华为技术有限公司")
                .responsiblePerson("张三")
                .processType("拉丝工艺")
                .productionMachine("拉丝机001")
                .finalEvaluationResult(WireMaterial.FinalEvaluationResult.FAIL)
                .eventTime(LocalDateTime.of(2024, 6, 15, 10, 30, 0))
                .build();
    }

    @Test
    void testGetProblematicBatches_WithISODateTimeFormat() {
        // 准备测试数据
        List<WireMaterial> mockResults = Arrays.asList(testWireMaterial);
        when(wireMaterialRepository.getFailedBatchesByDimension(
                eq("MANUFACTURER"), 
                eq("华为技术有限公司"), 
                any(LocalDateTime.class), 
                any(LocalDateTime.class)))
                .thenReturn(mockResults);

        // 执行测试 - 使用ISO格式的日期时间
        BaseResponse<List<BatchDetailResponse>> response = traceabilityService.getProblematicBatches(
                "MANUFACTURER", 
                "华为技术有限公司", 
                "2024-01-01T00:00:00", 
                "2026-12-31T23:59:59");

        // 验证结果
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals("TEST_BATCH_001", response.getData().get(0).getBatchNumber());

        // 验证repository方法被正确调用
        verify(wireMaterialRepository, times(1)).getFailedBatchesByDimension(
                eq("MANUFACTURER"), 
                eq("华为技术有限公司"), 
                any(LocalDateTime.class), 
                any(LocalDateTime.class));
    }

    @Test
    void testGetProblematicBatches_WithStandardDateTimeFormat() {
        // 准备测试数据
        List<WireMaterial> mockResults = Arrays.asList(testWireMaterial);
        when(wireMaterialRepository.getFailedBatchesByDimension(
                eq("RESPONSIBLE_PERSON"), 
                eq("张三"), 
                any(LocalDateTime.class), 
                any(LocalDateTime.class)))
                .thenReturn(mockResults);

        // 执行测试 - 使用标准格式的日期时间
        BaseResponse<List<BatchDetailResponse>> response = traceabilityService.getProblematicBatches(
                "RESPONSIBLE_PERSON", 
                "张三", 
                "2024-01-01 00:00:00", 
                "2026-12-31 23:59:59");

        // 验证结果
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void testGetProblematicBatches_WithNullDateTimes() {
        // 准备测试数据
        List<WireMaterial> mockResults = Arrays.asList(testWireMaterial);
        when(wireMaterialRepository.getFailedBatchesByDimension(
                eq("PROCESS_TYPE"), 
                eq("拉丝工艺"), 
                isNull(), 
                isNull()))
                .thenReturn(mockResults);

        // 执行测试 - 不传递时间参数
        BaseResponse<List<BatchDetailResponse>> response = traceabilityService.getProblematicBatches(
                "PROCESS_TYPE", 
                "拉丝工艺", 
                null, 
                null);

        // 验证结果
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());

        // 验证repository方法被正确调用，时间参数为null
        verify(wireMaterialRepository, times(1)).getFailedBatchesByDimension(
                eq("PROCESS_TYPE"), 
                eq("拉丝工艺"), 
                isNull(), 
                isNull());
    }

    @Test
    void testGetProblematicBatches_WithEmptyResults() {
        // 准备测试数据 - 空结果
        when(wireMaterialRepository.getFailedBatchesByDimension(
                anyString(), anyString(), any(), any()))
                .thenReturn(Collections.emptyList());

        // 执行测试
        BaseResponse<List<BatchDetailResponse>> response = traceabilityService.getProblematicBatches(
                "PRODUCTION_MACHINE", 
                "拉丝机999", 
                "2024-01-01T00:00:00", 
                "2024-12-31T23:59:59");

        // 验证结果
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        assertEquals(0, response.getData().size());
    }

    @Test
    void testGetProblematicBatches_WithInvalidParameters() {
        // 测试空的维度参数
        BaseResponse<List<BatchDetailResponse>> response1 = traceabilityService.getProblematicBatches(
                "", "华为技术有限公司", "2024-01-01T00:00:00", "2024-12-31T23:59:59");
        
        assertNotNull(response1);
        assertEquals("Error", response1.getCode());
        assertTrue(response1.getMsg().contains("维度和维度值不能为空"));

        // 测试空的维度值参数
        BaseResponse<List<BatchDetailResponse>> response2 = traceabilityService.getProblematicBatches(
                "MANUFACTURER", "", "2024-01-01T00:00:00", "2024-12-31T23:59:59");

        assertNotNull(response2);
        assertEquals("Error", response2.getCode());
        assertTrue(response2.getMsg().contains("维度和维度值不能为空"));
    }

    @Test
    void testGetProblematicBatches_WithInvalidDateRange() {
        // 测试开始时间晚于结束时间
        BaseResponse<List<BatchDetailResponse>> response = traceabilityService.getProblematicBatches(
                "MANUFACTURER", 
                "华为技术有限公司", 
                "2024-12-31T23:59:59", 
                "2024-01-01T00:00:00");
        
        assertNotNull(response);
        assertEquals("Error", response.getCode());
        assertTrue(response.getMsg().contains("开始时间不能晚于结束时间"));
    }

    @Test
    void testGetProblematicBatches_WithInvalidDateFormat() {
        // 准备测试数据
        List<WireMaterial> mockResults = Arrays.asList(testWireMaterial);
        when(wireMaterialRepository.getFailedBatchesByDimension(
                eq("MANUFACTURER"), 
                eq("华为技术有限公司"), 
                isNull(), // 无效格式会被解析为null
                isNull()))
                .thenReturn(mockResults);

        // 执行测试 - 使用无效的日期时间格式
        BaseResponse<List<BatchDetailResponse>> response = traceabilityService.getProblematicBatches(
                "MANUFACTURER", 
                "华为技术有限公司", 
                "invalid-date-format", 
                "another-invalid-format");

        // 验证结果 - 应该成功执行，但时间参数被解析为null
        assertNotNull(response);
        assertEquals("success", response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());

        // 验证repository方法被调用，时间参数为null
        verify(wireMaterialRepository, times(1)).getFailedBatchesByDimension(
                eq("MANUFACTURER"), 
                eq("华为技术有限公司"), 
                isNull(), 
                isNull());
    }

    @Test
    void testGetProblematicBatches_WithRepositoryException() {
        // 模拟repository抛出异常
        when(wireMaterialRepository.getFailedBatchesByDimension(
                anyString(), anyString(), any(), any()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试
        BaseResponse<List<BatchDetailResponse>> response = traceabilityService.getProblematicBatches(
                "MANUFACTURER", 
                "华为技术有限公司", 
                "2024-01-01T00:00:00", 
                "2024-12-31T23:59:59");

        // 验证结果
        assertNotNull(response);
        assertEquals("Error", response.getCode());
        assertTrue(response.getMsg().contains("获取问题批次详情失败"));
        assertTrue(response.getMsg().contains("数据库连接失败"));
    }
}
