package com.mmw.metal_micro_wire_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    /**
     * JWT签名密钥
     */
    private String secret;
    
    /**
     * 普通登录过期时间（小时）
     */
    private int expirationNormal;
    
    /**
     * 记住登录过期时间（小时）
     */
    private int expirationRemember;
} 