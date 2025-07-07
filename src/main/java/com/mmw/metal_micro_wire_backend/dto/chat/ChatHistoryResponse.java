package com.mmw.metal_micro_wire_backend.dto.chat;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天历史记录响应DTO
 */
@Data
public class ChatHistoryResponse {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 会话标题
     */
    private String title;
    
    /**
     * 消息列表
     */
    private List<ChatMessage> messages;
    
    /**
     * 消息总数
     */
    private int messageCount;
    
    /**
     * 会话创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 聊天消息
     */
    @Data
    public static class ChatMessage {
        /**
         * 消息ID
         */
        private String messageId;
        
        /**
         * 消息角色（user/assistant）
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
        
        /**
         * 消息时间
         */
        private LocalDateTime timestamp;
    }
} 