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
} 