package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.chat.ChatToolCall;

import java.util.List;

/**
 * AI工具调用服务接口
 */
public interface ChatToolService {
    
    /**
     * 获取所有可用的工具定义
     * @return 工具定义列表
     */
    List<ChatToolCall.Tool> getAvailableTools();
    
    /**
     * 执行工具调用
     * @param toolCall 工具调用请求
     * @param userId 用户ID
     * @return 工具调用结果
     */
    ChatToolCall.ToolCallResult executeToolCall(ChatToolCall.ToolCallRequest toolCall, Long userId);
} 