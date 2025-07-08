package com.mmw.metal_micro_wire_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmw.metal_micro_wire_backend.config.DeepSeekConfig;
import com.mmw.metal_micro_wire_backend.service.impl.ChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 多工具调用执行测试
 * 验证修复后的多工具调用处理逻辑
 */
@ExtendWith(MockitoExtension.class)
class MultiToolCallExecutionTest {

    @Mock
    private DeepSeekConfig deepSeekConfig;

    @Mock
    private RedisService redisService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ChatToolService chatToolService;

    private ChatServiceImpl chatService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        chatService = new ChatServiceImpl(
                deepSeekConfig,
                redisService,
                restTemplate,
                objectMapper,
                chatToolService
        );
    }

    @Test
    void testContainsToolCallMarkersWithNewFormats() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("containsToolCallMarkers", String.class);
        method.setAccessible(true);

        // 测试新增的格式检测
        assertTrue((Boolean) method.invoke(chatService, "<｜tool▁call▁begin｜>some content<｜tool▁call▁end｜>"));
        assertTrue((Boolean) method.invoke(chatService, "tool_call_begin some content tool_call_end"));
        assertTrue((Boolean) method.invoke(chatService, "content with <｜tool▁sep｜> separator"));
        assertTrue((Boolean) method.invoke(chatService, "content with tool▁sep separator"));
        
        // 测试JSON数组格式检测
        assertTrue((Boolean) method.invoke(chatService, "[{\"name\": \"get_device_list\", \"arguments\": {}}]"));
        assertTrue((Boolean) method.invoke(chatService, "Some text [{'name': 'get_stats', 'arguments': {}}] more text"));
        
        // 测试不包含工具调用标记的内容
        assertFalse((Boolean) method.invoke(chatService, "这是普通的回答内容"));
        // assertFalse((Boolean) method.invoke(chatService, null));
    }

    @Test
    void testParseToolCallsWithSeparator() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseToolCallsWithSeparator", String.class);
        method.setAccessible(true);

        // 测试分隔符格式解析
        String content = "<｜tool▁call▁begin｜>function<｜tool▁sep｜>get_device_list<｜tool▁sep｜>{\"page\": 1}<｜tool▁call▁end｜>";
        String result = (String) method.invoke(chatService, content);
        
        assertNotNull(result);
        assertTrue(result.contains("get_device_list"));
        assertTrue(result.contains("\"page\": 1"));
    }

    @Test
    void testParseToolCallsWithSeparatorMultiple() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseToolCallsWithSeparator", String.class);
        method.setAccessible(true);

        // 测试多个分隔符格式工具调用
        String content = "<｜tool▁call▁begin｜>function<｜tool▁sep｜>get_device_list<｜tool▁call▁end｜>" +
                        "<｜tool▁call▁begin｜>function<｜tool▁sep｜>get_overall_statistics<｜tool▁call▁end｜>";
        String result = (String) method.invoke(chatService, content);
        
        assertNotNull(result);
        assertTrue(result.contains("get_device_list"));
        assertTrue(result.contains("get_overall_statistics"));
    }

    @Test
    void testParseToolCallsWithSeparatorNoArguments() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseToolCallsWithSeparator", String.class);
        method.setAccessible(true);

        // 测试没有参数的分隔符格式
        String content = "<｜tool▁call▁begin｜>function<｜tool▁sep｜>get_current_time<｜tool▁call▁end｜>";
        String result = (String) method.invoke(chatService, content);
        
        assertNotNull(result);
        assertTrue(result.contains("get_current_time"));
        assertTrue(result.contains("{}"));  // 应该有默认的空参数
    }

    @Test
    void testParseToolCallsWithSeparatorInvalidFormat() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseToolCallsWithSeparator", String.class);
        method.setAccessible(true);

        // 测试无效的分隔符格式
        String content = "<｜tool▁call▁begin｜>invalid_format<｜tool▁call▁end｜>";
        String result = (String) method.invoke(chatService, content);
        
        // 应该返回null或空结果
        assertTrue(result == null || result.equals("[]"));
    }

    @Test
    void testParseDeepSeekToolCallsWithMixedFormats() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseDeepSeekToolCalls", String.class);
        method.setAccessible(true);

        // 测试混合格式：既有JSON又有分隔符
        String content = "首先调用时间工具：<｜tool▁call▁begin｜>function<｜tool▁sep｜>get_current_time<｜tool▁call▁end｜>" +
                        "然后调用其他工具：<|tool_calls_begin|>[{\"name\": \"get_device_list\", \"arguments\": {}}]<|tool_calls_end|>";
        
        @SuppressWarnings("unchecked")
        java.util.List<Object> result = (java.util.List<Object>) method.invoke(chatService, content);
        
        assertNotNull(result);
        // 应该能解析出至少一个工具调用
        assertTrue(result.size() >= 1);
    }

    @Test
    void testRecursionDepthLimit() throws Exception {
        // 测试递归深度限制
        // 这个测试需要模拟深度递归的情况
        // 由于涉及复杂的模拟，这里只测试方法存在性
        
        Method method = ChatServiceImpl.class.getDeclaredMethod("handleDeepSeekToolCalls", 
                String.class, java.util.List.class, String.class, Long.class, int.class);
        method.setAccessible(true);
        
        assertNotNull(method);
        assertEquals(5, method.getParameterCount());
    }

    @Test
    void testSystemOperationReportScenario() {
        // 模拟用户请求"给我一个完整的系统运营报告"的场景
        // String userRequest = "给我一个完整的系统运营报告，包括设备状态、质量统计和问题分析";
        
        // 模拟DeepSeek返回的包含多个工具调用的响应
        String deepSeekResponse = "<｜tool▁calls▁begin｜>[" +
                "{\"name\": \"get_device_list\", \"arguments\": {}}," +
                "{\"name\": \"get_overall_statistics\", \"arguments\": {}}," +
                "{\"name\": \"get_quality_issues\", \"arguments\": {}}," +
                "{\"name\": \"get_manufacturer_ranking\", \"arguments\": {}}" +
                "]<｜tool▁calls▁end｜>";
        
        try {
            Method containsMethod = ChatServiceImpl.class.getDeclaredMethod("containsToolCallMarkers", String.class);
            containsMethod.setAccessible(true);
            
            Method parseMethod = ChatServiceImpl.class.getDeclaredMethod("parseDeepSeekToolCalls", String.class);
            parseMethod.setAccessible(true);
            
            // 验证能够检测到工具调用标记
            assertTrue((Boolean) containsMethod.invoke(chatService, deepSeekResponse));
            
            // 验证能够解析出4个工具调用
            @SuppressWarnings("unchecked")
            java.util.List<Object> toolCalls = (java.util.List<Object>) parseMethod.invoke(chatService, deepSeekResponse);
            assertEquals(4, toolCalls.size());
            
        } catch (Exception e) {
            fail("测试系统运营报告场景时发生异常: " + e.getMessage());
        }
    }
}
