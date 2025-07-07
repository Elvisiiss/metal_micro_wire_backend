package com.mmw.metal_micro_wire_backend.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 聊天消息请求DTO
 */
@Data
public class ChatMessageRequest {
    
    /**
     * 会话ID（可选，如果为空则创建新会话）
     */
    private String sessionId;
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过4000字符")
    private String message;
    
    /**
     * 会话标题（仅在创建新会话时使用）
     */
    @Size(max = 50, message = "会话标题不能超过50字符")
    private String title;
} 