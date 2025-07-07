package com.mmw.metal_micro_wire_backend.dto.chat;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话响应DTO
 */
@Data
public class ChatSessionResponse {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 会话标题
     */
    private String title;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 消息数量
     */
    private Integer messageCount;
    
    /**
     * 最后一条消息
     */
    private String lastMessage;
} 