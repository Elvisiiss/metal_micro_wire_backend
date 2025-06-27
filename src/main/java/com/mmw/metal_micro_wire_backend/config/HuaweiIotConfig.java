package com.mmw.metal_micro_wire_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 华为云IoT统一配置类
 * 包含AMQP连接配置和消息处理配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "huawei.iot")
public class HuaweiIotConfig {
    
    /**
     * AMQP连接配置
     */
    private AmqpConfig amqp = new AmqpConfig();
    
    /**
     * 消息处理配置
     */
    private MessageConfig message = new MessageConfig();
    
    /**
     * AMQP连接配置内部类
     */
    @Data
    public static class AmqpConfig {
        /**
         * AMQP服务器地址
         */
        private String host;
        
        /**
         * AMQP服务器端口
         */
        private int port;
        
        /**
         * 访问密钥
         */
        private String accessKey;
        
        /**
         * 访问码
         */
        private String accessCode;
        
        /**
         * 默认队列名称
         */
        private String defaultQueue;
        
        /**
         * 队列预取数量
         */
        private int queuePrefetch = 100;
        
        /**
         * 是否自动确认消息
         */
        private boolean autoAcknowledge = true;
        
        /**
         * 重连延迟时间（毫秒）
         */
        private long reconnectDelay = 3000L;
        
        /**
         * 最大重连延迟时间（毫秒）
         */
        private long maxReconnectDelay = 30000L;
    }
    
    /**
     * 消息处理配置内部类
     */
    @Data
    public static class MessageConfig {
        /**
         * 是否启用详细日志记录
         */
        private boolean enableDetailedLogging = true;
        
        /**
         * 最大消息大小（字符数）
         */
        private int maxMessageSize = 1000000;
    }
} 