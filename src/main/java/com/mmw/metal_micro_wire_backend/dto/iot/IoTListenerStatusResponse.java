package com.mmw.metal_micro_wire_backend.dto.iot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT监听器状态响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IoTListenerStatusResponse {
    
    /**
     * 是否正在监听
     */
    private boolean isListening;
    
    /**
     * 监听器ID
     */
    private String listenerId;
    
    /**
     * 监听器启动时间
     */
    private Long startTime;
    
    /**
     * 消息统计信息
     */
    private MessageStats messageStats;
    
    /**
     * 消息统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageStats {
        /**
         * 总消息数
         */
        private long totalCount;
        
        /**
         * 成功处理数
         */
        private long successCount;
        
        /**
         * 失败处理数
         */
        private long failedCount;
        
        /**
         * 成功率（百分比）
         */
        private double successRate;
    }
} 