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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工具调用执行测试
 * 验证修复后的工具调用处理逻辑
 */
@ExtendWith(MockitoExtension.class)
class ToolCallExecutionTest {

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
    void testContainsToolCallMarkers() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("containsToolCallMarkers", String.class);
        method.setAccessible(true);

        // 测试各种工具调用标记格式
        assertTrue((Boolean) method.invoke(chatService, "<|tool_calls_begin|>some content<|tool_calls_end|>"));
        assertTrue((Boolean) method.invoke(chatService, "<｜tool▁calls▁begin｜>some content<｜tool▁calls▁end｜>"));
        assertTrue((Boolean) method.invoke(chatService, "tool_calls_begin some content tool_calls_end"));
        assertTrue((Boolean) method.invoke(chatService, "function_calls: [...]"));
        
        // 测试不包含工具调用标记的内容
        assertFalse((Boolean) method.invoke(chatService, "这是普通的回答内容"));
        assertFalse((Boolean) method.invoke(chatService, "没有工具调用的文本"));
        // assertFalse((Boolean) method.invoke(chatService, null));
    }

    @Test
    void testParseDeepSeekToolCallsWithStandardFormat() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseDeepSeekToolCalls", String.class);
        method.setAccessible(true);

        // 测试标准格式的工具调用
        String content = "<|tool_calls_begin|>[{\"name\": \"get_device_list\", \"arguments\": {\"page\": 1, \"size\": 10}}]<|tool_calls_end|>";
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) method.invoke(chatService, content);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testParseDeepSeekToolCallsWithMultipleTools() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseDeepSeekToolCalls", String.class);
        method.setAccessible(true);

        // 测试多个工具调用
        String content = "<|tool_calls_begin|>[" +
                "{\"name\": \"get_device_list\", \"arguments\": {}}," +
                "{\"name\": \"get_overall_statistics\", \"arguments\": {}}," +
                "{\"name\": \"get_quality_issues\", \"arguments\": {}}" +
                "]<|tool_calls_end|>";
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) method.invoke(chatService, content);
        
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void testParseDeepSeekToolCallsWithAlternativeFormat() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseDeepSeekToolCalls", String.class);
        method.setAccessible(true);

        // 测试替代格式的工具调用
        String content = "<｜tool▁calls▁begin｜>[{\"name\": \"get_manufacturer_ranking\", \"arguments\": {\"startTime\": \"2024-01-01 00:00:00\"}}]<｜tool▁calls▁end｜>";
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) method.invoke(chatService, content);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testParseDeepSeekToolCallsWithInvalidFormat() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseDeepSeekToolCalls", String.class);
        method.setAccessible(true);

        // 测试无效格式
        String content = "这是普通的文本内容，没有工具调用";
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) method.invoke(chatService, content);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testParseDeepSeekToolCallsWithMalformedJson() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseDeepSeekToolCalls", String.class);
        method.setAccessible(true);

        // 测试格式错误的JSON
        String content = "<|tool_calls_begin|>[{\"name\": \"get_device_list\", \"arguments\": invalid_json}]<|tool_calls_end|>";
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) method.invoke(chatService, content);
        
        assertNotNull(result);
        assertEquals(0, result.size()); // 应该返回空列表而不是抛出异常
    }

    @Test
    void testParseDeepSeekToolCallsWithNestedFunction() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseDeepSeekToolCalls", String.class);
        method.setAccessible(true);

        // 测试嵌套函数格式
        String content = "<|tool_calls_begin|>[{\"function\": {\"name\": \"analyze_quality_issues\", \"arguments\": \"{\\\"dimension\\\": \\\"manufacturer\\\", \\\"dimensionValue\\\": \\\"华为\\\"}\"}}]<|tool_calls_end|>";
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) method.invoke(chatService, content);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testParseDeepSeekToolCallsWithParametersField() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseDeepSeekToolCalls", String.class);
        method.setAccessible(true);

        // 测试使用parameters字段的格式
        String content = "<|tool_calls_begin|>[{\"name\": \"get_wire_material_list\", \"parameters\": {\"page\": 1, \"manufacturer\": \"华为\"}}]<|tool_calls_end|>";
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) method.invoke(chatService, content);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testParseDeepSeekToolCallsWithDirectJsonArray() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseDeepSeekToolCalls", String.class);
        method.setAccessible(true);

        // 测试直接的JSON数组格式（没有标记）
        String content = "我需要调用以下工具：[{\"name\": \"get_current_time\", \"arguments\": {\"format\": \"datetime\"}}] 来获取当前时间。";
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) method.invoke(chatService, content);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
