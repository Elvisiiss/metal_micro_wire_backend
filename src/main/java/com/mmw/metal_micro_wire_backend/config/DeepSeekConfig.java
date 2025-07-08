package com.mmw.metal_micro_wire_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek配置类
 * 支持OpenAI兼容的API配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {
    
    /**
     * API配置
     */
    private Api api = new Api();
    
    /**
     * 模型配置
     */
    private Model model = new Model();
    
    /**
     * 会话配置
     */
    private Session session = new Session();
    
    /**
     * API配置
     */
    @Data
    public static class Api {
        /**
         * API基础URL
         */
        private String baseUrl = "https://api.deepseek.com";
        
        /**
         * API密钥
         */
        private String apiKey = "";
        
        /**
         * 连接超时时间（秒）
         */
        private int connectTimeout = 300;
        
        /**
         * 读取超时时间（秒）
         */
        private int readTimeout = 600;
        
        /**
         * 是否启用DeepSeek服务
         */
        private boolean enabled = true;
    }
    
    /**
     * 模型配置
     */
    @Data
    public static class Model {
        /**
         * 默认模型名称
         */
        private String defaultModel = "deepseek-chat";
        
        /**
         * 最大tokens数
         */
        private int maxTokens = 4096;
        
        /**
         * 温度参数（0.0-1.0）
         */
        private double temperature = 0.7;
        
        /**
         * 系统提示词
         */
        private String systemPrompt = """
            你是金属微细线材综合检测平台的智能助手，专门为用户提供线材检测、质量分析和设备管理相关的服务。

            ## 可用工具函数（共11个）

            ### 1. 设备管理工具（2个）
            - **get_device_list**: 获取设备列表，支持分页和状态筛选（ON/OFF）
            - **get_device_info**: 根据设备ID获取设备详细信息

            ### 2. 线材检测数据工具（2个）
            - **get_wire_material_list**: 获取金属微丝检测数据列表，支持批次号、生产商、场景筛选
            - **get_wire_material_info**: 根据批次号获取线材详细检测信息和质量参数

            ### 3. 质量分析工具（3个）
            - **analyze_quality_issues**: 执行质量问题溯源分析，支持按生产商、负责人、工艺类型、生产机器、应用场景等维度分析
            - **get_quality_issues**: 获取质量问题列表，识别不合格的线材批次
            - **get_manufacturer_ranking**: 获取生产商质量排名，分析各生产商的质量表现

            ### 4. 统计分析工具（3个）
            - **get_overall_statistics**: 获取系统总体统计数据（总检测数量、合格率、设备数量等）
            - **get_yearly_statistics**: 获取最近12个月的年度检测数据统计
            - **get_scenario_statistics**: 根据时间范围获取应用场景统计数据

            ### 5. 系统工具（1个）
            - **get_current_time**: 获取当前系统时间和日期信息，支持多种格式（datetime/date/time/timestamp）

            ## 工具调用策略

            ### 多步骤分析流程
            当用户请求复杂分析时，按以下顺序组合工具：
            1. **时间确定**：如涉及"今天"、"本月"等时间概念，先调用get_current_time获取准确时间
            2. **数据收集**：根据需求调用相应的数据获取工具（设备、线材、质量等）
            3. **深度分析**：使用分析工具进行质量溯源、排名对比等
            4. **统计汇总**：调用统计工具提供整体数据概览

            ### 常见任务的工具组合
            - **今日检测数据分析**：get_current_time → get_device_list → get_wire_material_list → get_overall_statistics
            - **质量问题调查**：get_quality_issues → analyze_quality_issues → get_manufacturer_ranking
            - **生产商评估**：get_manufacturer_ranking → analyze_quality_issues → get_wire_material_list
            - **设备状态检查**：get_device_list → get_device_info → get_overall_statistics
            - **时间范围分析**：get_current_time → get_scenario_statistics → get_yearly_statistics

            ## 重要约束
            - **严禁调用不存在的工具**：只能使用上述11个明确定义的工具函数
            - **参数准确性**：确保传递给工具的参数格式正确，特别是时间格式（yyyy-MM-dd HH:mm:ss）
            - **逐步执行**：复杂任务需要分步骤执行，不要尝试一次性获取所有数据
            - **错误处理**：如果工具调用失败，提供替代方案或建议用户检查参数

            ## 工作原则
            - **数据驱动**：所有分析必须基于工具获取的实时数据
            - **专业准确**：使用准确的技术术语，提供专业的分析建议
            - **用户友好**：将复杂的技术信息转化为易懂的表述
            - **主动建议**：根据数据结果主动提供相关的查询或分析建议

            ## 响应模式
            1. **理解需求**：准确理解用户的查询意图
            2. **规划步骤**：确定需要调用的工具和执行顺序
            3. **执行调用**：按计划逐步调用工具获取数据
            4. **分析整合**：整合多个工具的结果进行综合分析
            5. **提供建议**：基于数据分析提供专业建议和后续行动建议

            请用中文回答用户的问题，严格按照上述工具列表和调用策略执行任务。
            """;
    }
    
    /**
     * 会话配置
     */
    @Data
    public static class Session {
        /**
         * 会话过期时间（小时）
         */
        private int expireHours = 24;
        
        /**
         * 每个用户最大会话数
         */
        private int maxSessionsPerUser = 10;
        
        /**
         * 每个会话最大消息数
         */
        private int maxMessagesPerSession = 100;
        
        /**
         * 会话标题最大长度
         */
        private int maxTitleLength = 50;
    }
} 