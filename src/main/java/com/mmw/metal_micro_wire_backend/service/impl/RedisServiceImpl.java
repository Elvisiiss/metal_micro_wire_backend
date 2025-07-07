package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.config.VerificationConfig;
import com.mmw.metal_micro_wire_backend.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    
    private final StringRedisTemplate redisTemplate;
    private final VerificationConfig verificationConfig;
    
    // 验证码前缀
    private static final String CODE_PREFIX = "verification_code:";
    // 验证码发送冷却前缀
    private static final String CODE_COOLDOWN_PREFIX = "code_cooldown:";
    // 聊天会话前缀
    private static final String CHAT_SESSION_PREFIX = "chat_session:";
    // 聊天消息历史前缀
    private static final String CHAT_MESSAGE_HISTORY_PREFIX = "chat_message_history:";
    // 用户会话列表前缀
    private static final String USER_SESSIONS_PREFIX = "user_sessions:";
    
    @Override
    public void saveVerificationCode(String email, String code, String type) {
        String key = CODE_PREFIX + type + ":" + email;
        redisTemplate.opsForValue().set(key, code, verificationConfig.getCodeExpireMinutes(), TimeUnit.MINUTES);
        
        // 设置发送冷却时间
        setCodeSendCooldown(email, type, verificationConfig.getSendCooldownSeconds());
        
        log.info("验证码已存储到Redis，邮箱：{}，类型：{}，有效期：{}分钟，冷却时间：{}秒", 
                email, type, verificationConfig.getCodeExpireMinutes(), verificationConfig.getSendCooldownSeconds());
    }
    
    @Override
    public boolean verifyCode(String email, String code, String type) {
        String key = CODE_PREFIX + type + ":" + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            log.warn("验证码不存在或已过期，邮箱：{}，类型：{}", email, type);
            return false;
        }
        
        boolean isValid = storedCode.equals(code);
        if (isValid) {
            // 验证成功后删除验证码和冷却时间
            redisTemplate.delete(key);
            String cooldownKey = CODE_COOLDOWN_PREFIX + type + ":" + email;
            redisTemplate.delete(cooldownKey);
            log.info("验证码验证成功并已删除，邮箱：{}，类型：{}", email, type);
        } else {
            log.warn("验证码验证失败，邮箱：{}，类型：{}，输入：{}，存储：{}", email, type, code, storedCode);
        }
        
        return isValid;
    }
    
    @Override
    public boolean isCodeSent(String email, String type) {
        // 检查是否在冷却时间内，而不是检查验证码是否存在
        return getCodeSendCooldown(email, type) > 0;
    }
    
    @Override
    public long getCodeSendCooldown(String email, String type) {
        String cooldownKey = CODE_COOLDOWN_PREFIX + type + ":" + email;
        Long ttl = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
    
    @Override
    public void setCodeSendCooldown(String email, String type, int cooldownSeconds) {
        String cooldownKey = CODE_COOLDOWN_PREFIX + type + ":" + email;
        redisTemplate.opsForValue().set(cooldownKey, "1", cooldownSeconds, TimeUnit.SECONDS);
    }
    
    @Override
    public void deleteCode(String email, String type) {
        String key = CODE_PREFIX + type + ":" + email;
        String cooldownKey = CODE_COOLDOWN_PREFIX + type + ":" + email;
        redisTemplate.delete(key);
        redisTemplate.delete(cooldownKey);
        log.info("验证码和冷却时间已删除，邮箱：{}，类型：{}", email, type);
    }
    
    @Override
    public void setWithExpiration(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
        log.debug("数据已存储到Redis，键：{}，过期时间：{} {}", key, timeout, unit);
    }
    
    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("数据已从Redis删除，键：{}", key);
    }
    
    @Override
    public boolean exists(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }
    
    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    // ==================== 聊天会话管理实现 ====================
    
    @Override
    public void saveChatSession(Long userId, String sessionId, String sessionData, int expireHours) {
        String sessionKey = CHAT_SESSION_PREFIX + userId + ":" + sessionId;
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        
        // 保存会话数据
        redisTemplate.opsForValue().set(sessionKey, sessionData, expireHours, TimeUnit.HOURS);
        
        // 添加会话ID到用户会话列表
        redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        redisTemplate.expire(userSessionsKey, expireHours, TimeUnit.HOURS);
        
        log.debug("聊天会话已保存，用户ID：{}，会话ID：{}，过期时间：{}小时", userId, sessionId, expireHours);
    }
    
    @Override
    public String getChatSession(Long userId, String sessionId) {
        String sessionKey = CHAT_SESSION_PREFIX + userId + ":" + sessionId;
        return redisTemplate.opsForValue().get(sessionKey);
    }
    
    @Override
    public void deleteChatSession(Long userId, String sessionId) {
        String sessionKey = CHAT_SESSION_PREFIX + userId + ":" + sessionId;
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        String messageHistoryKey = CHAT_MESSAGE_HISTORY_PREFIX + userId + ":" + sessionId;
        
        // 删除会话数据
        redisTemplate.delete(sessionKey);
        
        // 从用户会话列表中删除
        redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
        
        // 删除消息历史
        redisTemplate.delete(messageHistoryKey);
        
        log.debug("聊天会话已删除，用户ID：{}，会话ID：{}", userId, sessionId);
    }
    
    @Override
    public java.util.Set<String> getUserChatSessions(Long userId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        return redisTemplate.opsForSet().members(userSessionsKey);
    }
    
    @Override
    public void saveChatMessageHistory(Long userId, String sessionId, String messageHistory, int expireHours) {
        String messageHistoryKey = CHAT_MESSAGE_HISTORY_PREFIX + userId + ":" + sessionId;
        redisTemplate.opsForValue().set(messageHistoryKey, messageHistory, expireHours, TimeUnit.HOURS);
        
        log.debug("聊天消息历史已保存，用户ID：{}，会话ID：{}，过期时间：{}小时", userId, sessionId, expireHours);
    }
    
    @Override
    public String getChatMessageHistory(Long userId, String sessionId) {
        String messageHistoryKey = CHAT_MESSAGE_HISTORY_PREFIX + userId + ":" + sessionId;
        return redisTemplate.opsForValue().get(messageHistoryKey);
    }
    
    @Override
    public void deleteChatMessageHistory(Long userId, String sessionId) {
        String messageHistoryKey = CHAT_MESSAGE_HISTORY_PREFIX + userId + ":" + sessionId;
        redisTemplate.delete(messageHistoryKey);
        
        log.debug("聊天消息历史已删除，用户ID：{}，会话ID：{}", userId, sessionId);
    }
    
    @Override
    public int getUserChatSessionCount(Long userId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        Long count = redisTemplate.opsForSet().size(userSessionsKey);
        return count != null ? count.intValue() : 0;
    }
    
    @Override
    public void cleanupExpiredChatSessions(Long userId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        java.util.Set<String> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);
        
        if (sessionIds != null && !sessionIds.isEmpty()) {
            for (String sessionId : sessionIds) {
                String sessionKey = CHAT_SESSION_PREFIX + userId + ":" + sessionId;
                
                // 检查会话是否过期
                if (!redisTemplate.hasKey(sessionKey)) {
                    // 会话已过期，从用户会话列表中删除
                    redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
                    
                    // 删除对应的消息历史
                    String messageHistoryKey = CHAT_MESSAGE_HISTORY_PREFIX + userId + ":" + sessionId;
                    redisTemplate.delete(messageHistoryKey);
                    
                    log.debug("清理过期会话，用户ID：{}，会话ID：{}", userId, sessionId);
                }
            }
        }
    }
} 