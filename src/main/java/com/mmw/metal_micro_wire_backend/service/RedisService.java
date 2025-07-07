package com.mmw.metal_micro_wire_backend.service;

import java.util.concurrent.TimeUnit;

/**
 * Redis服务接口
 */
public interface RedisService {
    
    /**
     * 存储验证码
     * @param email 邮箱
     * @param code 验证码
     * @param type 类型（register、login、reset）
     */
    void saveVerificationCode(String email, String code, String type);
    
    /**
     * 验证验证码
     * @param email 邮箱
     * @param code 验证码
     * @param type 类型（register、login、reset）
     * @return 是否验证成功
     */
    boolean verifyCode(String email, String code, String type);
    
    /**
     * 检查验证码是否已发送（防止频繁发送）
     * @param email 邮箱
     * @param type 类型
     * @return 是否已发送
     */
    boolean isCodeSent(String email, String type);
    
    /**
     * 检查验证码发送冷却时间
     * @param email 邮箱
     * @param type 类型
     * @return 剩余冷却时间（秒），0表示可以发送
     */
    long getCodeSendCooldown(String email, String type);
    
    /**
     * 设置验证码发送冷却时间
     * @param email 邮箱
     * @param type 类型
     * @param cooldownSeconds 冷却时间（秒）
     */
    void setCodeSendCooldown(String email, String type, int cooldownSeconds);
    
    /**
     * 删除验证码
     * @param email 邮箱
     * @param type 类型
     */
    void deleteCode(String email, String type);
    
    /**
     * 存储键值对并设置过期时间
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    void setWithExpiration(String key, String value, long timeout, TimeUnit unit);
    
    /**
     * 删除键
     * @param key 键
     */
    void delete(String key);
    
    /**
     * 检查键是否存在
     * @param key 键
     * @return 是否存在
     */
    boolean exists(String key);
    
    /**
     * 获取键对应的值
     * @param key 键
     * @return 值，如果不存在返回null
     */
    String get(String key);
    
    // ==================== 聊天会话管理 ====================
    
    /**
     * 保存聊天会话
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param sessionData 会话数据JSON
     * @param expireHours 过期时间（小时）
     */
    void saveChatSession(Long userId, String sessionId, String sessionData, int expireHours);
    
    /**
     * 获取聊天会话
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 会话数据JSON
     */
    String getChatSession(Long userId, String sessionId);
    
    /**
     * 删除聊天会话
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void deleteChatSession(Long userId, String sessionId);
    
    /**
     * 获取用户的所有会话ID
     * @param userId 用户ID
     * @return 会话ID列表
     */
    java.util.Set<String> getUserChatSessions(Long userId);
    
    /**
     * 保存聊天消息历史
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param messageHistory 消息历史JSON
     * @param expireHours 过期时间（小时）
     */
    void saveChatMessageHistory(Long userId, String sessionId, String messageHistory, int expireHours);
    
    /**
     * 获取聊天消息历史
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 消息历史JSON
     */
    String getChatMessageHistory(Long userId, String sessionId);
    
    /**
     * 删除聊天消息历史
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void deleteChatMessageHistory(Long userId, String sessionId);
    
    /**
     * 获取用户会话数量
     * @param userId 用户ID
     * @return 会话数量
     */
    int getUserChatSessionCount(Long userId);
    
    /**
     * 清理用户过期会话
     * @param userId 用户ID
     */
    void cleanupExpiredChatSessions(Long userId);
} 