package com.mmw.metal_micro_wire_backend.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建聊天会话请求DTO
 */
@Data
public class CreateChatSessionRequest {
    
    /**
     * 会话标题
     */
    @NotBlank(message = "会话标题不能为空")
    @Size(max = 50, message = "会话标题不能超过50字符")
    private String title;
} 