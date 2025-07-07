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
        private String systemPrompt = "你是一个智能助手，请用中文回答用户的问题。";
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