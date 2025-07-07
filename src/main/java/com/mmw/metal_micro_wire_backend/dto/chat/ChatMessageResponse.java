package com.mmw.metal_micro_wire_backend.dto.chat;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息响应DTO
 */
@Data
public class ChatMessageResponse {
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 用户消息
     */
    private String userMessage;
    
    /**
     * 助手回复
     */
    private String assistantMessage;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 是否是新会话
     */
    private boolean isNewSession;
    
    /**
     * 会话标题
     */
    private String sessionTitle;
} 