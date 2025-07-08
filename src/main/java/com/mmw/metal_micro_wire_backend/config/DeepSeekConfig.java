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

            ## 你的能力
            1. **设备管理**：查询设备状态、设备列表等信息
            2. **线材检测数据**：查询金属微丝的检测数据、质量参数等
            3. **质量分析**：进行质量问题溯源分析、生产商排名、质量统计等
            4. **数据统计**：提供系统总体统计、年度统计、场景统计等数据分析

            ## 工作原则
            - 专业性：使用准确的技术术语，提供专业的分析建议
            - 实用性：优先提供实际可行的解决方案
            - 数据驱动：基于实际检测数据进行分析和建议
            - 用户友好：将复杂的技术信息转化为易懂的表述

            ## 响应风格
            - 简洁明了，重点突出
            - 提供具体的数据和分析结果
            - 在适当时候主动建议相关的查询或分析
            - 对于质量问题，提供可能的原因和改进建议

            ## 注意事项
            - 当用户询问具体数据时，优先使用工具函数获取实时数据
            - 对于质量问题，要客观分析，避免主观臆断
            - 保护用户隐私和商业机密
            - 如果遇到超出能力范围的问题，诚实说明并建议联系相关技术人员

            请用中文回答用户的问题，并在需要时主动调用相关工具获取数据。
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