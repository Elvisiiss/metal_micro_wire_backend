package com.mmw.metal_micro_wire_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmw.metal_micro_wire_backend.dto.chat.ChatToolCall;
import com.mmw.metal_micro_wire_backend.service.impl.ChatToolServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatToolService测试类
 */
@ExtendWith(MockitoExtension.class)
class ChatToolServiceTest {

    @Mock
    private DeviceService deviceService;

    @Mock
    private WireMaterialManageService wireMaterialManageService;

    @Mock
    private TraceabilityService traceabilityService;

    @Mock
    private OverViewService overViewService;

    private ChatToolServiceImpl chatToolService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        chatToolService = new ChatToolServiceImpl(
                deviceService,
                wireMaterialManageService,
                traceabilityService,
                overViewService,
                objectMapper
        );
    }

    @Test
    void testGetAvailableTools() {
        // 测试获取可用工具列表
        List<ChatToolCall.Tool> tools = chatToolService.getAvailableTools();
        
        assertNotNull(tools);
        assertFalse(tools.isEmpty());
        
        // 验证包含时间工具
        boolean hasTimeTool = tools.stream()
                .anyMatch(tool -> "get_current_time".equals(tool.getFunction().getName()));
        assertTrue(hasTimeTool, "应该包含get_current_time工具");
        
        // 验证工具总数（应该是11个：2个设备+2个线材+3个质量+3个统计+1个时间）
        assertEquals(11, tools.size(), "应该有11个工具");
    }

    @Test
    void testGetCurrentTimeTool() {
        // 测试时间工具的定义
        List<ChatToolCall.Tool> tools = chatToolService.getAvailableTools();
        
        ChatToolCall.Tool timeTool = tools.stream()
                .filter(tool -> "get_current_time".equals(tool.getFunction().getName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(timeTool, "应该找到时间工具");
        assertEquals("get_current_time", timeTool.getFunction().getName());
        assertEquals("获取当前系统时间和日期信息", timeTool.getFunction().getDescription());
        assertNotNull(timeTool.getFunction().getParameters());
    }

    @Test
    void testExecuteGetCurrentTimeWithDefaultFormat() throws Exception {
        // 测试执行时间工具调用（默认格式）
        ChatToolCall.ToolCallRequest request = new ChatToolCall.ToolCallRequest();
        request.setId("test_call_1");
        request.setType("function");
        
        ChatToolCall.FunctionCall functionCall = new ChatToolCall.FunctionCall();
        functionCall.setName("get_current_time");
        functionCall.setArguments("{}");
        request.setFunction(functionCall);
        
        ChatToolCall.ToolCallResult result = chatToolService.executeToolCall(request, 1L);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        
        // 验证返回的JSON包含时间信息
        JsonNode resultJson = objectMapper.readTree(result.getResult());
        assertTrue(resultJson.has("currentTime"));
        assertTrue(resultJson.has("timestamp"));
        assertTrue(resultJson.has("year"));
        assertTrue(resultJson.has("month"));
        assertTrue(resultJson.has("day"));
    }

    @Test
    void testExecuteGetCurrentTimeWithDateFormat() throws Exception {
        // 测试执行时间工具调用（日期格式）
        ChatToolCall.ToolCallRequest request = new ChatToolCall.ToolCallRequest();
        request.setId("test_call_2");
        request.setType("function");
        
        ChatToolCall.FunctionCall functionCall = new ChatToolCall.FunctionCall();
        functionCall.setName("get_current_time");
        functionCall.setArguments("{\"format\": \"date\"}");
        request.setFunction(functionCall);
        
        ChatToolCall.ToolCallResult result = chatToolService.executeToolCall(request, 1L);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        
        // 验证返回的JSON包含日期信息
        JsonNode resultJson = objectMapper.readTree(result.getResult());
        assertTrue(resultJson.has("formattedTime"));
        assertEquals("当前日期", resultJson.get("description").asText());
    }

    @Test
    void testExecuteGetCurrentTimeWithTimestampFormat() throws Exception {
        // 测试执行时间工具调用（时间戳格式）
        ChatToolCall.ToolCallRequest request = new ChatToolCall.ToolCallRequest();
        request.setId("test_call_3");
        request.setType("function");
        
        ChatToolCall.FunctionCall functionCall = new ChatToolCall.FunctionCall();
        functionCall.setName("get_current_time");
        functionCall.setArguments("{\"format\": \"timestamp\"}");
        request.setFunction(functionCall);
        
        ChatToolCall.ToolCallResult result = chatToolService.executeToolCall(request, 1L);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        
        // 验证返回的JSON包含时间戳信息
        JsonNode resultJson = objectMapper.readTree(result.getResult());
        assertTrue(resultJson.has("formattedTime"));
        assertEquals("当前时间戳", resultJson.get("description").asText());
        
        // 验证时间戳是数字
        String timestamp = resultJson.get("formattedTime").asText();
        assertDoesNotThrow(() -> Long.parseLong(timestamp));
    }
}
