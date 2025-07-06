package com.mmw.metal_micro_wire_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 通知配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.notification")
public class NotificationConfig {
    
    /**
     * 是否启用邮件通知
     */
    private boolean emailEnabled = true;
    
    /**
     * 质量问题不合格率阈值（百分比）
     */
    private double failRateThreshold = 5.0;
    
    /**
     * 严重问题不合格率阈值（百分比）
     */
    private double criticalFailRateThreshold = 10.0;
    
    /**
     * 高风险问题不合格率阈值（百分比）
     */
    private double highRiskFailRateThreshold = 8.0;
    
    /**
     * 中等风险问题不合格率阈值（百分比）
     */
    private double mediumRiskFailRateThreshold = 6.0;
    
    /**
     * 管理员邮箱列表（用于接收汇总报告）
     */
    private String[] adminEmails = {};
    
    /**
     * 邮件发送重试次数
     */
    private int emailRetryCount = 3;
    
    /**
     * 邮件发送重试间隔（毫秒）
     */
    private long emailRetryInterval = 5000;
    
    /**
     * 是否启用质量监控定时任务
     */
    private boolean qualityMonitorEnabled = true;
    
    /**
     * 质量监控定时任务执行频率（cron表达式）
     */
    private String qualityMonitorCron = "0 0 * * * ?"; // 每小时执行一次
    
    /**
     * 质量报告生成频率（cron表达式）
     */
    private String qualityReportCron = "0 0 2 * * ?"; // 每天凌晨2点执行
}
