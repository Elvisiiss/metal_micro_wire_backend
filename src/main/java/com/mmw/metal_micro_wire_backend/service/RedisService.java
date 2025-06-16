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
} 