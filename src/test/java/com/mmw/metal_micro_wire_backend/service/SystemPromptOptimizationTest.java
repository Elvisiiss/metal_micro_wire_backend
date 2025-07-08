package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.config.DeepSeekConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统提示词优化测试
 * 验证优化后的系统提示词是否包含正确的工具指导信息
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
class SystemPromptOptimizationTest {

    private DeepSeekConfig deepSeekConfig;

    @BeforeEach
    void setUp() {
        deepSeekConfig = new DeepSeekConfig();
    }

    @Test
    void testSystemPromptContainsAllToolFunctions() {
        String systemPrompt = deepSeekConfig.getModel().getSystemPrompt();
        
        assertNotNull(systemPrompt, "系统提示词不能为空");
        
        // 验证包含所有11个工具函数
        String[] expectedTools = {
            "get_device_list",
            "get_device_info", 
            "get_wire_material_list",
            "get_wire_material_info",
            "analyze_quality_issues",
            "get_quality_issues",
            "get_manufacturer_ranking",
            "get_overall_statistics",
            "get_yearly_statistics",
            "get_scenario_statistics",
            "get_current_time"
        };
        
        for (String tool : expectedTools) {
            assertTrue(systemPrompt.contains(tool), 
                "系统提示词应该包含工具函数: " + tool);
        }
    }

    @Test
    void testSystemPromptContainsToolCategories() {
        String systemPrompt = deepSeekConfig.getModel().getSystemPrompt();
        
        // 验证包含工具分类
        assertTrue(systemPrompt.contains("设备管理工具"), "应该包含设备管理工具分类");
        assertTrue(systemPrompt.contains("线材检测数据工具"), "应该包含线材检测数据工具分类");
        assertTrue(systemPrompt.contains("质量分析工具"), "应该包含质量分析工具分类");
        assertTrue(systemPrompt.contains("统计分析工具"), "应该包含统计分析工具分类");
        assertTrue(systemPrompt.contains("系统工具"), "应该包含系统工具分类");
    }

    @Test
    void testSystemPromptContainsMultiStepStrategy() {
        String systemPrompt = deepSeekConfig.getModel().getSystemPrompt();
        
        // 验证包含多步骤分析策略
        assertTrue(systemPrompt.contains("多步骤分析流程"), "应该包含多步骤分析流程指导");
        assertTrue(systemPrompt.contains("工具调用策略"), "应该包含工具调用策略");
        assertTrue(systemPrompt.contains("常见任务的工具组合"), "应该包含常见任务的工具组合示例");
    }

    @Test
    void testSystemPromptContainsConstraints() {
        String systemPrompt = deepSeekConfig.getModel().getSystemPrompt();
        
        // 验证包含重要约束
        assertTrue(systemPrompt.contains("严禁调用不存在的工具"), "应该包含工具调用约束");
        assertTrue(systemPrompt.contains("参数准确性"), "应该包含参数准确性要求");
        assertTrue(systemPrompt.contains("逐步执行"), "应该包含逐步执行指导");
        assertTrue(systemPrompt.contains("错误处理"), "应该包含错误处理指导");
    }

    @Test
    void testSystemPromptContainsSpecificTaskExamples() {
        String systemPrompt = deepSeekConfig.getModel().getSystemPrompt();
        
        // 验证包含具体任务示例
        assertTrue(systemPrompt.contains("今日检测数据分析"), "应该包含今日检测数据分析示例");
        assertTrue(systemPrompt.contains("get_current_time → get_device_list → get_wire_material_list → get_overall_statistics"), 
            "应该包含今日检测数据分析的工具调用序列");
        
        assertTrue(systemPrompt.contains("质量问题调查"), "应该包含质量问题调查示例");
        assertTrue(systemPrompt.contains("生产商评估"), "应该包含生产商评估示例");
        assertTrue(systemPrompt.contains("设备状态检查"), "应该包含设备状态检查示例");
    }

    @Test
    void testSystemPromptContainsResponseMode() {
        String systemPrompt = deepSeekConfig.getModel().getSystemPrompt();
        
        // 验证包含响应模式指导
        assertTrue(systemPrompt.contains("响应模式"), "应该包含响应模式指导");
        assertTrue(systemPrompt.contains("理解需求"), "应该包含理解需求步骤");
        assertTrue(systemPrompt.contains("规划步骤"), "应该包含规划步骤指导");
        assertTrue(systemPrompt.contains("执行调用"), "应该包含执行调用指导");
        assertTrue(systemPrompt.contains("分析整合"), "应该包含分析整合指导");
        assertTrue(systemPrompt.contains("提供建议"), "应该包含提供建议指导");
    }

    @Test
    void testSystemPromptLength() {
        String systemPrompt = deepSeekConfig.getModel().getSystemPrompt();
        
        // 验证系统提示词长度合理（不能太短也不能太长）
        assertTrue(systemPrompt.length() > 1000, "系统提示词应该足够详细");
        assertTrue(systemPrompt.length() < 10000, "系统提示词不应该过长影响性能");
    }

    @Test
    void testSystemPromptStructure() {
        String systemPrompt = deepSeekConfig.getModel().getSystemPrompt();
        
        // 验证系统提示词结构
        assertTrue(systemPrompt.contains("##"), "应该包含标题结构");
        assertTrue(systemPrompt.contains("###"), "应该包含子标题结构");
        assertTrue(systemPrompt.contains("-"), "应该包含列表结构");
        assertTrue(systemPrompt.contains("**"), "应该包含强调格式");
    }

    @Test
    void testSystemPromptContainsToolCount() {
        String systemPrompt = deepSeekConfig.getModel().getSystemPrompt();
        
        // 验证明确说明工具数量
        assertTrue(systemPrompt.contains("共11个"), "应该明确说明工具总数为11个");
        assertTrue(systemPrompt.contains("2个"), "应该说明设备管理工具有2个");
        assertTrue(systemPrompt.contains("3个"), "应该说明质量分析工具有3个");
        assertTrue(systemPrompt.contains("1个"), "应该说明系统工具有1个");
    }

    @Test
    void testSystemPromptContainsChineseInstructions() {
        String systemPrompt = deepSeekConfig.getModel().getSystemPrompt();
        
        // 验证包含中文回答指导
        assertTrue(systemPrompt.contains("用中文回答"), "应该包含中文回答指导");
        assertTrue(systemPrompt.contains("严格按照上述工具列表"), "应该包含严格执行指导");
    }
}
