package com.mmw.metal_micro_wire_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 验证码配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "verification")
public class VerificationConfig {
    
    /**
     * 验证码有效期（分钟）
     */
    private int codeExpireMinutes;
    
    /**
     * 验证码发送冷却时间（秒）
     */
    private int sendCooldownSeconds;
} 