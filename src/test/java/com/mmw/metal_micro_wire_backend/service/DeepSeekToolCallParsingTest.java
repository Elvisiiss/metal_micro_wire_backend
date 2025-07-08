package com.mmw.metal_micro_wire_backend.service;

// import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmw.metal_micro_wire_backend.dto.chat.ChatToolCall;
import com.mmw.metal_micro_wire_backend.service.impl.ChatServiceImpl;
// import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeepSeek工具调用解析测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class DeepSeekToolCallParsingTest {

    @Autowired
    private ChatServiceImpl chatService;

    // private ObjectMapper objectMapper;

    // @BeforeEach
    // void setUp() {
    //     objectMapper = new ObjectMapper();
    // }

    @Test
    void testParseDeepSeekToolCallsWithMarkdownFormat() throws Exception {
        // 测试实际的DeepSeek返回格式
        String content = "<｜tool▁calls▁begin｜><｜tool▁call▁begin｜>function<｜tool▁sep｜>get_device_list\n" +
                "```json\n" +
                "{\"status\":\"ON\"}\n" +
                "```<｜tool▁call▁end｜>\n" +
                "<｜tool▁call▁begin｜>function<｜tool▁sep｜>get_overall_statistics\n" +
                "```json\n" +
                "{}\n" +
                "```<｜tool▁call▁end｜>\n" +
                "<｜tool▁call▁begin｜>function<｜tool▁sep｜>get_quality_issues\n" +
                "```json\n" +
                "{}\n" +
                "```<｜tool▁call▁end｜>\n" +
                "<｜tool▁call▁begin｜>function<｜tool▁sep｜>get_manufacturer_ranking\n" +
                "```json\n" +
                "{}\n" +
                "```<｜tool▁call▁end｜><｜tool▁calls▁end｜>";

        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("parseDeepSeekToolCalls", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<ChatToolCall.ToolCallRequest> result = (List<ChatToolCall.ToolCallRequest>) method.invoke(chatService, content);

        assertNotNull(result);
        assertEquals(4, result.size());

        // 验证第一个工具调用
        ChatToolCall.ToolCallRequest firstCall = result.get(0);
        assertEquals("get_device_list", firstCall.getFunction().getName());
        assertEquals("{\"status\":\"ON\"}", firstCall.getFunction().getArguments());

        // 验证第二个工具调用
        ChatToolCall.ToolCallRequest secondCall = result.get(1);
        assertEquals("get_overall_statistics", secondCall.getFunction().getName());
        assertEquals("{}", secondCall.getFunction().getArguments());

        // 验证第三个工具调用
        ChatToolCall.ToolCallRequest thirdCall = result.get(2);
        assertEquals("get_quality_issues", thirdCall.getFunction().getName());
        assertEquals("{}", thirdCall.getFunction().getArguments());

        // 验证第四个工具调用
        ChatToolCall.ToolCallRequest fourthCall = result.get(3);
        assertEquals("get_manufacturer_ranking", fourthCall.getFunction().getName());
        assertEquals("{}", fourthCall.getFunction().getArguments());
    }

    @Test
    void testExtractJsonFromMarkdown() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("extractJsonFromMarkdown", String.class);
        method.setAccessible(true);

        // 测试标准markdown格式
        String markdownJson = "```json\n{\"status\":\"ON\"}\n```";
        String result = (String) method.invoke(chatService, markdownJson);
        assertEquals("{\"status\":\"ON\"}", result);

        // 测试简单代码块格式
        String codeBlockJson = "```\n{\"page\": 1}\n```";
        String result2 = (String) method.invoke(chatService, codeBlockJson);
        assertEquals("{\"page\": 1}", result2);

        // 测试纯JSON格式
        String pureJson = "{\"test\": true}";
        String result3 = (String) method.invoke(chatService, pureJson);
        assertEquals("{\"test\": true}", result3);

        // 测试空内容
        String emptyContent = "";
        String result4 = (String) method.invoke(chatService, emptyContent);
        assertEquals("{}", result4);

        // 测试非JSON内容
        String nonJson = "```\nsome text\n```";
        String result5 = (String) method.invoke(chatService, nonJson);
        assertEquals("{}", result5);
    }

    @Test
    void testIsValidJson() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("isValidJson", String.class);
        method.setAccessible(true);

        // 测试有效JSON
        assertTrue((Boolean) method.invoke(chatService, "{}"));
        assertTrue((Boolean) method.invoke(chatService, "{\"key\": \"value\"}"));
        assertTrue((Boolean) method.invoke(chatService, "[]"));
        assertTrue((Boolean) method.invoke(chatService, "[{\"name\": \"test\"}]"));

        // 测试无效JSON
        assertFalse((Boolean) method.invoke(chatService, "{invalid}"));
        assertFalse((Boolean) method.invoke(chatService, "not json"));
        assertTrue((Boolean) method.invoke(chatService, "")); // 空字符串被Jackson认为是有效的
        assertFalse((Boolean) method.invoke(chatService, (Object) null));
    }

    @Test
    void testTryFixJsonFormat() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("tryFixJsonFormat", String.class);
        method.setAccessible(true);

        // 测试包含HTML标签的JSON
        String htmlJson = "<div>{\"key\": \"value\"}</div>";
        String result = (String) method.invoke(chatService, htmlJson);
        assertEquals("{\"key\": \"value\"}", result);

        // 测试转义字符问题
        String escapedJson = "{\\\"key\\\": \\\"value\\\"}";
        String result2 = (String) method.invoke(chatService, escapedJson);
        assertEquals("{\"key\": \"value\"}", result2);

        // 测试缺少结尾的数组
        String incompleteArray = "[{\"name\": \"test\"}";
        String result3 = (String) method.invoke(chatService, incompleteArray);
        assertEquals("[{\"name\": \"test\"}]", result3);

        // 测试无法修复的内容
        String unfixable = "completely broken content";
        String result4 = (String) method.invoke(chatService, unfixable);
        assertNull(result4);
    }

    @Test
    void testContainsToolCallMarkers() throws Exception {
        // 使用反射访问私有方法
        Method method = ChatServiceImpl.class.getDeclaredMethod("containsToolCallMarkers", String.class);
        method.setAccessible(true);

        // 测试各种工具调用标记
        assertTrue((Boolean) method.invoke(chatService, "<｜tool▁calls▁begin｜>"));
        assertTrue((Boolean) method.invoke(chatService, "<｜tool▁call▁begin｜>"));
        assertTrue((Boolean) method.invoke(chatService, "<｜tool▁sep｜>"));
        assertTrue((Boolean) method.invoke(chatService, "<|tool_calls_begin|>"));
        assertTrue((Boolean) method.invoke(chatService, "tool_calls_begin"));
        assertTrue((Boolean) method.invoke(chatService, "function_calls"));

        // 测试JSON数组格式
        assertTrue((Boolean) method.invoke(chatService, "[{\"name\": \"test\", \"arguments\": {}}]"));

        // 测试不包含标记的内容
        assertFalse((Boolean) method.invoke(chatService, "normal text"));
        assertFalse((Boolean) method.invoke(chatService, ""));
        assertFalse((Boolean) method.invoke(chatService, (Object) null));
    }
}
