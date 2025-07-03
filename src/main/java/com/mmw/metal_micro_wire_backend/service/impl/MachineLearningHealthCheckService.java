package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.service.MachineLearningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 机器学习模型健康检查服务
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MachineLearningHealthCheckService {
    
    private final MachineLearningService machineLearningService;
    
    @Value("${ml.model.enabled:true}")
    private boolean mlModelEnabled;
    
    /**
     * 应用启动后检查机器学习模型健康状态
     */
    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        if (!mlModelEnabled) {
            log.info("🔄 机器学习模型功能已禁用");
            return;
        }
        
        log.info("🔍 开始检查机器学习模型服务健康状态...");
        
        try {
            boolean healthy = machineLearningService.checkHealth();
            if (healthy) {
                log.info("机器学习模型服务连接正常");
                log.info("质量评估功能：规则引擎 + 机器学习模型 双重保障");
            } else {
                log.warn("机器学习模型服务连接失败");
                log.warn("质量评估功能：仅使用规则引擎评估");
            }
        } catch (Exception e) {
            log.error("❌ 机器学习模型服务检查失败", e);
            log.warn("🔄 质量评估功能：仅使用规则引擎评估");
        }
    }
    
    /**
     * 定期检查机器学习模型健康状态
     * 每5分钟检查一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300,000毫秒
    public void scheduledHealthCheck() {
        if (!mlModelEnabled) {
            return;
        }
        
        try {
            boolean healthy = machineLearningService.checkHealth();
            if (!healthy) {
                log.warn("定期检查：机器学习模型服务不可用");
            }
        } catch (Exception e) {
            log.debug("定期健康检查失败", e);
        }
    }
} 