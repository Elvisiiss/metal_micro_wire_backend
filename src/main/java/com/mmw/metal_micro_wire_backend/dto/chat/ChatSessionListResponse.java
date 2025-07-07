package com.mmw.metal_micro_wire_backend.dto.chat;

import lombok.Data;

import java.util.List;

/**
 * 聊天会话列表响应DTO
 */
@Data
public class ChatSessionListResponse {
    
    /**
     * 会话列表
     */
    private List<ChatSessionResponse> sessions;
    
    /**
     * 总数
     */
    private int total;
} 