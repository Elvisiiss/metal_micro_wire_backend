package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.chat.*;

/**
 * 聊天服务接口
 */
public interface ChatService {
    
    /**
     * 发送聊天消息
     * @param userId 用户ID
     * @param userType 用户类型
     * @param request 聊天消息请求
     * @return 聊天消息响应
     */
    ChatMessageResponse sendMessage(Long userId, TokenService.UserType userType, ChatMessageRequest request);
    
    /**
     * 创建新的聊天会话
     * @param userId 用户ID
     * @param userType 用户类型
     * @param request 创建会话请求
     * @return 会话响应
     */
    ChatSessionResponse createSession(Long userId, TokenService.UserType userType, CreateChatSessionRequest request);
    
    /**
     * 获取用户的聊天会话列表
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 会话列表响应
     */
    ChatSessionListResponse getUserSessions(Long userId, TokenService.UserType userType);
    
    /**
     * 获取聊天历史记录
     * @param userId 用户ID
     * @param userType 用户类型
     * @param sessionId 会话ID
     * @return 聊天历史响应
     */
    ChatHistoryResponse getChatHistory(Long userId, TokenService.UserType userType, String sessionId);
    
    /**
     * 删除聊天会话
     * @param userId 用户ID
     * @param userType 用户类型
     * @param sessionId 会话ID
     */
    void deleteSession(Long userId, TokenService.UserType userType, String sessionId);
    
    /**
     * 更新会话标题
     * @param userId 用户ID
     * @param userType 用户类型
     * @param sessionId 会话ID
     * @param title 新标题
     */
    void updateSessionTitle(Long userId, TokenService.UserType userType, String sessionId, String title);
    
    /**
     * 清理用户的过期会话
     * @param userId 用户ID
     * @param userType 用户类型
     */
    void cleanupExpiredSessions(Long userId, TokenService.UserType userType);
} 