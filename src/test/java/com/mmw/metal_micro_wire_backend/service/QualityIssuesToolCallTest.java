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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 质量问题工具调用测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class QualityIssuesToolCallTest {

    @Mock
    private TraceabilityService traceabilityService;

    @InjectMocks
    private ChatToolServiceImpl chatToolService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testExecuteGetQualityIssuesWithoutParameters() throws Exception {
        // 模拟空参数调用
        String jsonArgs = "{}";
        JsonNode args = objectMapper.readTree(jsonArgs);

        // 模拟服务返回
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
    }

    @Test
    void testExecuteGetQualityIssuesWithValidDimension() throws Exception {
        // 测试有效的维度参数
        String jsonArgs = "{\"dimension\": \"manufacturer\", \"dimensionValue\": \"测试生产商\"}";
        JsonNode args = objectMapper.readTree(jsonArgs);

        // 模拟服务返回
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
    }

    @Test
    void testExecuteGetQualityIssuesWithInvalidDimension() throws Exception {
        // 测试无效的维度参数
        String jsonArgs = "{\"dimension\": \"invalid_dimension\"}";
        JsonNode args = objectMapper.readTree(jsonArgs);

        // 模拟服务返回
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
    void testExecuteGetQualityIssuesWithNullDimension() throws Exception {
        // 测试null维度参数
        String jsonArgs = "{\"dimension\": null}";
        JsonNode args = objectMapper.readTree(jsonArgs);

        // 模拟服务返回
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
    }

    @Test
    void testExecuteGetQualityIssuesWithEmptyDimension() throws Exception {
        // 测试空字符串维度参数
        String jsonArgs = "{\"dimension\": \"\"}";
        JsonNode args = objectMapper.readTree(jsonArgs);

        // 模拟服务返回
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
    }

    @Test
    void testExecuteGetQualityIssuesWithAllDimensions() throws Exception {
        // 测试所有有效维度
        String[] dimensions = {"manufacturer", "responsible_person", "process_type", "production_machine"};
        
        for (String dimension : dimensions) {
            String jsonArgs = "{\"dimension\": \"" + dimension + "\"}";
            JsonNode args = objectMapper.readTree(jsonArgs);

            // 模拟服务返回
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
    void testExecuteGetQualityIssuesWithNoIssuesFound() throws Exception {
        // 测试没有发现质量问题的情况
        String jsonArgs = "{\"dimension\": \"manufacturer\"}";
        JsonNode args = objectMapper.readTree(jsonArgs);

        // 模拟服务返回空列表
        List<QualityIssueResponse> mockIssues = new ArrayList<>();
        BaseResponse<List<QualityIssueResponse>> mockResponse = BaseResponse.success(mockIssues);
        when(traceabilityService.identifyQualityIssues(any(TraceabilityQueryRequest.class)))
                .thenReturn(mockResponse);

        // 使用反射调用私有方法
        Method method = ChatToolServiceImpl.class.getDeclaredMethod("executeGetQualityIssues", JsonNode.class);
        method.setAccessible(true);

        String result = (String) method.invoke(chatToolService, args);

        assertNotNull(result);
        assertTrue(result.contains("未发现质量问题") || result.contains("符合质量标准"));
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
    }
}
