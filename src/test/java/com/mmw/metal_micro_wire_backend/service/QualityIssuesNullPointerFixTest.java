package com.mmw.metal_micro_wire_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.QualityIssueResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.TraceabilityQueryRequest;
import com.mmw.metal_micro_wire_backend.service.impl.ChatToolServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 质量问题工具调用空指针修复测试
 */
public class QualityIssuesNullPointerFixTest {

    @Mock
    private TraceabilityService traceabilityService;

    @InjectMocks
    private ChatToolServiceImpl chatToolService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        
        // 手动注入ObjectMapper到ChatToolServiceImpl
        Field objectMapperField = ChatToolServiceImpl.class.getDeclaredField("objectMapper");
        objectMapperField.setAccessible(true);
        objectMapperField.set(chatToolService, objectMapper);
    }

    @Test
    void testExecuteGetQualityIssuesWithoutParameters() throws Exception {
        // 测试空参数调用
        String jsonArgs = "{}";
        JsonNode args = objectMapper.readTree(jsonArgs);

        // 模拟服务返回成功响应
        List<QualityIssueResponse> mockIssues = new ArrayList<>();
        QualityIssueResponse issue = new QualityIssueResponse();
        issue.setDimensionValue("测试生产商");
        issue.setDescription("质量问题测试");
        mockIssues.add(issue);

        BaseResponse<List<QualityIssueResponse>> mockResponse = BaseResponse.success(mockIssues);
        when(traceabilityService.identifyQualityIssues(any(TraceabilityQueryRequest.class)))
                .thenReturn(mockResponse);

        // 使用反射调用私有方法
        Method method = ChatToolServiceImpl.class.getDeclaredMethod("executeGetQualityIssues", JsonNode.class);
        method.setAccessible(true);

        String result = (String) method.invoke(chatToolService, args);

        assertNotNull(result);
        assertFalse(result.contains("NullPointerException"));
        assertFalse(result.contains("获取质量问题出错"));
        assertFalse(result.contains("服务响应为空"));
    }

    @Test
    void testExecuteGetQualityIssuesWithNullServiceResponse() throws Exception {
        // 测试服务返回null的情况
        String jsonArgs = "{\"dimension\": \"manufacturer\"}";
        JsonNode args = objectMapper.readTree(jsonArgs);

        // 模拟服务返回null
        when(traceabilityService.identifyQualityIssues(any(TraceabilityQueryRequest.class)))
                .thenReturn(null);

        // 使用反射调用私有方法
        Method method = ChatToolServiceImpl.class.getDeclaredMethod("executeGetQualityIssues", JsonNode.class);
        method.setAccessible(true);

        String result = (String) method.invoke(chatToolService, args);

        assertNotNull(result);
        assertTrue(result.contains("获取质量问题失败：服务响应为空"));
        assertFalse(result.contains("NullPointerException"));
    }

    @Test
    void testExecuteGetQualityIssuesWithValidDimension() throws Exception {
        // 测试有效的维度参数
        String jsonArgs = "{\"dimension\": \"manufacturer\", \"dimensionValue\": \"测试生产商\"}";
        JsonNode args = objectMapper.readTree(jsonArgs);

        // 模拟服务返回成功响应
        List<QualityIssueResponse> mockIssues = new ArrayList<>();
        BaseResponse<List<QualityIssueResponse>> mockResponse = BaseResponse.success(mockIssues);
        when(traceabilityService.identifyQualityIssues(any(TraceabilityQueryRequest.class)))
                .thenReturn(mockResponse);

        // 使用反射调用私有方法
        Method method = ChatToolServiceImpl.class.getDeclaredMethod("executeGetQualityIssues", JsonNode.class);
        method.setAccessible(true);

        String result = (String) method.invoke(chatToolService, args);

        assertNotNull(result);
        assertFalse(result.contains("NullPointerException"));
        assertFalse(result.contains("获取质量问题出错"));
        assertTrue(result.contains("未发现质量问题") || result.contains("符合质量标准"));
    }

    @Test
    void testExecuteGetQualityIssuesWithInvalidDimension() throws Exception {
        // 测试无效的维度参数
        String jsonArgs = "{\"dimension\": \"invalid_dimension\"}";
        JsonNode args = objectMapper.readTree(jsonArgs);

        // 模拟服务返回成功响应
        List<QualityIssueResponse> mockIssues = new ArrayList<>();
        BaseResponse<List<QualityIssueResponse>> mockResponse = BaseResponse.success(mockIssues);
        when(traceabilityService.identifyQualityIssues(any(TraceabilityQueryRequest.class)))
                .thenReturn(mockResponse);

        // 使用反射调用私有方法
        Method method = ChatToolServiceImpl.class.getDeclaredMethod("executeGetQualityIssues", JsonNode.class);
        method.setAccessible(true);

        String result = (String) method.invoke(chatToolService, args);

        assertNotNull(result);
        assertFalse(result.contains("NullPointerException"));
        assertFalse(result.contains("获取质量问题出错"));
        // 应该使用默认维度MANUFACTURER
    }

    @Test
    void testExecuteGetQualityIssuesWithServiceError() throws Exception {
        // 测试服务返回错误的情况
        String jsonArgs = "{\"dimension\": \"manufacturer\"}";
        JsonNode args = objectMapper.readTree(jsonArgs);

        // 模拟服务返回错误
        BaseResponse<List<QualityIssueResponse>> mockResponse = BaseResponse.error("数据库连接失败");
        when(traceabilityService.identifyQualityIssues(any(TraceabilityQueryRequest.class)))
                .thenReturn(mockResponse);

        // 使用反射调用私有方法
        Method method = ChatToolServiceImpl.class.getDeclaredMethod("executeGetQualityIssues", JsonNode.class);
        method.setAccessible(true);

        String result = (String) method.invoke(chatToolService, args);

        assertNotNull(result);
        assertTrue(result.contains("获取质量问题失败"));
        assertTrue(result.contains("数据库连接失败"));
        assertFalse(result.contains("NullPointerException"));
    }

    @Test
    void testExecuteGetQualityIssuesWithAllDimensions() throws Exception {
        // 测试所有有效维度
        String[] dimensions = {"manufacturer", "responsible_person", "process_type", "production_machine"};
        
        for (String dimension : dimensions) {
            String jsonArgs = "{\"dimension\": \"" + dimension + "\"}";
            JsonNode args = objectMapper.readTree(jsonArgs);

            // 模拟服务返回成功响应
            List<QualityIssueResponse> mockIssues = new ArrayList<>();
            BaseResponse<List<QualityIssueResponse>> mockResponse = BaseResponse.success(mockIssues);
            when(traceabilityService.identifyQualityIssues(any(TraceabilityQueryRequest.class)))
                    .thenReturn(mockResponse);

            // 使用反射调用私有方法
            Method method = ChatToolServiceImpl.class.getDeclaredMethod("executeGetQualityIssues", JsonNode.class);
            method.setAccessible(true);

            String result = (String) method.invoke(chatToolService, args);

            assertNotNull(result, "维度 " + dimension + " 应该返回非null结果");
            assertFalse(result.contains("NullPointerException"), "维度 " + dimension + " 不应该出现NullPointerException");
            assertFalse(result.contains("获取质量问题出错"), "维度 " + dimension + " 不应该出现错误");
        }
    }

    @Test
    void testExecuteGetQualityIssuesWithNullAndEmptyDimensions() throws Exception {
        // 测试null和空字符串维度
        String[] testCases = {
            "{\"dimension\": null}",
            "{\"dimension\": \"\"}",
            "{\"dimension\": \"   \"}"
        };
        
        for (String jsonArgs : testCases) {
            JsonNode args = objectMapper.readTree(jsonArgs);

            // 模拟服务返回成功响应
            List<QualityIssueResponse> mockIssues = new ArrayList<>();
            BaseResponse<List<QualityIssueResponse>> mockResponse = BaseResponse.success(mockIssues);
            when(traceabilityService.identifyQualityIssues(any(TraceabilityQueryRequest.class)))
                    .thenReturn(mockResponse);

            // 使用反射调用私有方法
            Method method = ChatToolServiceImpl.class.getDeclaredMethod("executeGetQualityIssues", JsonNode.class);
            method.setAccessible(true);

            String result = (String) method.invoke(chatToolService, args);

            assertNotNull(result, "测试用例 " + jsonArgs + " 应该返回非null结果");
            assertFalse(result.contains("NullPointerException"), "测试用例 " + jsonArgs + " 不应该出现NullPointerException");
            assertFalse(result.contains("获取质量问题出错"), "测试用例 " + jsonArgs + " 不应该出现错误");
        }
    }

    @Test
    void testExecuteGetQualityIssuesWithQualityIssuesFound() throws Exception {
        // 测试发现质量问题的情况
        String jsonArgs = "{\"dimension\": \"manufacturer\"}";
        JsonNode args = objectMapper.readTree(jsonArgs);

        // 模拟服务返回包含质量问题的响应
        List<QualityIssueResponse> mockIssues = new ArrayList<>();
        QualityIssueResponse issue1 = new QualityIssueResponse();
        issue1.setDimensionValue("问题生产商1");
        issue1.setDescription("质量问题1");
        mockIssues.add(issue1);
        
        QualityIssueResponse issue2 = new QualityIssueResponse();
        issue2.setDimensionValue("问题生产商2");
        issue2.setDescription("质量问题2");
        mockIssues.add(issue2);

        BaseResponse<List<QualityIssueResponse>> mockResponse = BaseResponse.success(mockIssues);
        when(traceabilityService.identifyQualityIssues(any(TraceabilityQueryRequest.class)))
                .thenReturn(mockResponse);

        // 使用反射调用私有方法
        Method method = ChatToolServiceImpl.class.getDeclaredMethod("executeGetQualityIssues", JsonNode.class);
        method.setAccessible(true);

        String result = (String) method.invoke(chatToolService, args);

        assertNotNull(result);
        assertFalse(result.contains("NullPointerException"));
        assertFalse(result.contains("获取质量问题出错"));
        assertFalse(result.contains("未发现质量问题"));
        // 结果应该是JSON格式的质量问题列表
        assertTrue(result.startsWith("[") && result.endsWith("]"));
    }
}
