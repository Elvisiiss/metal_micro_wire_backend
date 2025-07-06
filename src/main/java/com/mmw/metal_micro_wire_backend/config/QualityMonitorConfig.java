package com.mmw.metal_micro_wire_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 质量监控配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.quality-monitor")
public class QualityMonitorConfig {
    
    /**
     * 是否启用质量监控定时任务
     */
    private boolean enabled = true;
    
    /**
     * 定时任务执行频率（cron表达式）
     * 默认每天凌晨0点执行
     */
    private String cron = "0 0 0 * * ?";
    
    /**
     * 检测时间窗口（小时）
     * 默认检测前24小时的数据
     */
    private int detectionWindowHours = 24;
    
    /**
     * 是否向管理员发送无问题确认邮件
     */
    private boolean sendNoIssueNotificationToAdmin = true;
    
    /**
     * 质量问题判定阈值（百分比）
     */
    private double failRateThreshold = 5.0;
}
