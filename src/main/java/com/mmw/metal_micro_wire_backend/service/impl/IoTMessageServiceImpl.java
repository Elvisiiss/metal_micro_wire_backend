package com.mmw.metal_micro_wire_backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmw.metal_micro_wire_backend.config.HuaweiIotConfig;
import com.mmw.metal_micro_wire_backend.service.IoTMessageService;
import com.mmw.metal_micro_wire_backend.util.HuaweiIotAmqpUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IoT消息处理服务实现类
 * 采用灵活的消息处理方式，避免固定DTO结构的限制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IoTMessageServiceImpl implements IoTMessageService {
    
    private final HuaweiIotAmqpUtil huaweiIotAmqpUtil;
    private final HuaweiIotConfig huaweiIotConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String currentListenerId;
    
    // 消息统计
    private final AtomicLong totalMessageCount = new AtomicLong(0);
    private final AtomicLong successMessageCount = new AtomicLong(0);
    private final AtomicLong failedMessageCount = new AtomicLong(0);
    
    @Override
    public void processRawMessage(String rawMessage) {
        totalMessageCount.incrementAndGet();
        
        try {
            HuaweiIotConfig.MessageConfig messageConfig = huaweiIotConfig.getMessage();
            
            // 检查消息大小
            if (rawMessage.length() > messageConfig.getMaxMessageSize()) {
                log.warn("消息大小超过限制: {} > {}", rawMessage.length(), messageConfig.getMaxMessageSize());
                failedMessageCount.incrementAndGet();
                return;
            }
            
            if (messageConfig.isEnableDetailedLogging()) {
                log.info("收到IoT原始消息，大小: {} 字符", rawMessage.length());
                log.info("消息内容: {}", rawMessage);
            }
            
            // 先尝试解析为JSON，验证消息格式是否正确
            JsonNode messageNode = objectMapper.readTree(rawMessage);
            
            // 记录消息的基本信息
            if (messageConfig.isEnableDetailedLogging()) {
                logMessageInfo(messageNode);
            }
            
            // 调用灵活的消息处理方法
            processFlexibleMessage(rawMessage, messageNode);
            
            successMessageCount.incrementAndGet();
            
        } catch (Exception e) {
            failedMessageCount.incrementAndGet();
            log.error("处理IoT原始消息失败", e);
            if (huaweiIotConfig.getMessage().isEnableDetailedLogging()) {
                log.error("失败的消息内容: {}", rawMessage);
            }
            
            // 消息解析失败时，仍然保存原始消息供后续分析
            handleParseFailedMessage(rawMessage, e);
        }
        
        // 定期输出统计信息（每100条消息）
        if (totalMessageCount.get() % 100 == 0) {
            printMessageStats();
        }
    }
    
    /**
     * 记录消息的基本信息
     */
    private void logMessageInfo(JsonNode messageNode) {
        try {
            // 尝试获取一些通用字段，但不强制要求
            String resource = getTextValue(messageNode, "resource");
            String event = getTextValue(messageNode, "event");
            String eventTime = getTextValue(messageNode, "event_time");
            
            log.info("消息基本信息 - 资源类型: {}, 事件类型: {}, 事件时间: {}", 
                    resource != null ? resource : "未知", 
                    event != null ? event : "未知", 
                    eventTime != null ? eventTime : "未知");
            
            // 尝试获取设备信息
            JsonNode notifyData = messageNode.get("notify_data");
            if (notifyData != null) {
                JsonNode header = notifyData.get("header");
                if (header != null) {
                    String deviceId = getTextValue(header, "device_id");
                    String productId = getTextValue(header, "product_id");
                    log.info("设备信息 - 设备ID: {}, 产品ID: {}", 
                            deviceId != null ? deviceId : "未知",
                            productId != null ? productId : "未知");
                }
            }
        } catch (Exception e) {
            log.debug("获取消息基本信息时出现异常，继续处理", e);
        }
    }
    
    /**
     * 灵活的消息处理方法
     */
    private void processFlexibleMessage(String rawMessage, JsonNode messageNode) {
        try {
            // 这里可以根据不同的消息类型进行不同的处理
            // 但暂时只记录和保存原始消息
            
            log.info("开始处理消息，消息大小: {} 字符", rawMessage.length());
            
            // TODO: 在这里添加具体的业务处理逻辑
            // 例如：
            // 1. 将消息保存到数据库
            // 2. 发送到消息队列进行异步处理
            // 3. 根据消息内容触发特定的业务逻辑
            // 4. 进行数据统计和分析
            
            // 示例：检查是否是属性上报消息
            if (isPropertyReportMessage(messageNode)) {
                handlePropertyReportMessage(rawMessage, messageNode);
            } else {
                handleOtherMessage(rawMessage, messageNode);
            }
            
            log.info("消息处理完成");
            
        } catch (Exception e) {
            log.error("灵活消息处理失败", e);
        }
    }
    
    /**
     * 检查是否是属性上报消息
     */
    private boolean isPropertyReportMessage(JsonNode messageNode) {
        try {
            String resource = getTextValue(messageNode, "resource");
            String event = getTextValue(messageNode, "event");
            return "device.property".equals(resource) && "report".equals(event);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 处理属性上报消息
     */
    private void handlePropertyReportMessage(String rawMessage, JsonNode messageNode) {
        log.info("检测到属性上报消息，开始处理...");
        
        try {
            // 尝试提取服务数据，但不强制要求特定格式
            JsonNode notifyData = messageNode.get("notify_data");
            if (notifyData != null) {
                JsonNode body = notifyData.get("body");
                if (body != null) {
                    JsonNode services = body.get("services");
                    if (services != null && services.isArray()) {
                        log.info("找到 {} 个服务数据", services.size());
                        for (int i = 0; i < services.size(); i++) {
                            JsonNode service = services.get(i);
                            String serviceId = getTextValue(service, "service_id");
                            JsonNode properties = service.get("properties");
                            log.info("服务 {}: ID={}, 属性数量={}", 
                                    i + 1, 
                                    serviceId != null ? serviceId : "未知",
                                    properties != null ? properties.size() : 0);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取属性上报详细信息时出现异常，但不影响消息处理", e);
        }
        
        // TODO: 添加属性上报的具体业务逻辑
    }
    
    /**
     * 处理其他类型消息
     */
    private void handleOtherMessage(String rawMessage, JsonNode messageNode) {
        log.info("处理其他类型消息");
        // TODO: 添加其他消息类型的处理逻辑
    }
    
    /**
     * 处理解析失败的消息
     */
    private void handleParseFailedMessage(String rawMessage, Exception error) {
        log.warn("消息解析失败: {}", error.getMessage());
        
        // TODO: 可以将解析失败的消息保存到数据库或发送到死信队列
    }
    
    /**
     * 安全地获取JSON节点的文本值
     */
    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }
    
    /**
     * 打印消息统计信息
     */
    private void printMessageStats() {
        long total = totalMessageCount.get();
        long success = successMessageCount.get();
        long failed = failedMessageCount.get();
        double successRate = total > 0 ? (double) success / total * 100 : 0;
        
        log.info("IoT消息处理统计 - 总计: {}, 成功: {}, 失败: {}, 成功率: {:.2f}%", 
                total, success, failed, successRate);
    }
    
    @Override
    public void startMessageListener() {
        try {
            log.info("启动IoT消息监听器...");
            CompletableFuture<String> future = huaweiIotAmqpUtil.startDefaultQueueListener(this::processRawMessage);
            
            future.thenAccept(listenerId -> {
                this.currentListenerId = listenerId;
                log.info("IoT消息监听器启动成功，ID: {}", listenerId);
            }).exceptionally(throwable -> {
                log.error("启动IoT消息监听器失败", throwable);
                return null;
            });
        } catch (Exception e) {
            log.error("启动监听器异常", e);
        }
    }
    
    @Override
    public void stopMessageListener() {
        try {
            if (currentListenerId != null) {
                huaweiIotAmqpUtil.stopListener(currentListenerId);
                currentListenerId = null;
                log.info("IoT消息监听器已停止");
            }
        } catch (Exception e) {
            log.error("停止监听器异常", e);
        }
    }
    
    @PreDestroy
    public void cleanup() {
        log.info("清理IoT消息监听器...");
        stopMessageListener();
        huaweiIotAmqpUtil.stopAllListeners();
    }
} 