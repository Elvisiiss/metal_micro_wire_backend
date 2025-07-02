package com.mmw.metal_micro_wire_backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmw.metal_micro_wire_backend.config.HuaweiIotConfig;
import com.mmw.metal_micro_wire_backend.dto.iot.IoTListenerStatusResponse;
import com.mmw.metal_micro_wire_backend.service.IoTDataService;
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
    private final IoTDataService ioTDataService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String currentListenerId;
    private Long listenerStartTime;
    
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
            if (huaweiIotConfig.getMessage().isEnableDetailedLogging()) {
                log.info("开始处理消息，消息大小: {} 字符", rawMessage.length());
                log.info("原始消息内容: {}", rawMessage);
            }
            
            
            // TODO: 在这里添加具体的业务处理逻辑
            // 例如：
            // 1. 将消息保存到数据库
            // 2. 发送到消息队列进行异步处理
            // 3. 根据消息内容触发特定的业务逻辑
            // 4. 进行数据统计和分析
            
            // 检查是否是属性上报消息
            if (isPropertyReportMessage(messageNode)) {
                handlePropertyReportMessage(rawMessage, messageNode);
            }
            
            if (huaweiIotConfig.getMessage().isEnableDetailedLogging()) {
                log.info("消息处理完成");
            }
            
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
            // 提取消息类型
            String messageType = extractMessageType(messageNode);
            if (messageType == null) {
                log.warn("未找到消息类型，跳过处理");
                return;
            }
            
            if (huaweiIotConfig.getMessage().isEnableDetailedLogging()) {
                log.info("消息类型: {}", messageType);
            }
            
            // 根据消息类型进行不同的处理
            switch (messageType.toLowerCase()) {
                case "detection":
                    // 检测数据 - 保存线材信息
                    try {
                        ioTDataService.parseAndSaveWireMaterial(messageNode);
                        if (huaweiIotConfig.getMessage().isEnableDetailedLogging()) {
                            log.info("成功处理检测数据消息");
                        }
                    } catch (Exception e) {
                        log.error("处理检测数据消息失败", e);
                    }
                    break;
                    
                case "status":
                    // 状态数据 - 保存设备状态
                    try {
                        ioTDataService.parseAndSaveDevice(messageNode);
                        if (huaweiIotConfig.getMessage().isEnableDetailedLogging()) {
                            log.info("成功处理设备状态消息");
                        }
                    } catch (Exception e) {
                        log.error("处理设备状态消息失败", e);
                    }
                    break;
                    
                case "question":
                    // 问题数据 - 保存问题信息
                    try {
                        ioTDataService.parseAndSaveQuestion(messageNode);
                        if (huaweiIotConfig.getMessage().isEnableDetailedLogging()) {
                            log.info("成功处理问题消息");
                        }
                    } catch (Exception e) {
                        log.error("处理问题消息失败", e);
                    }
                    break;
                    
                default:
                    log.warn("未知的消息类型: {}", messageType);
                    break;
            }
            
        } catch (Exception e) {
            log.error("处理属性上报消息时出现异常", e);
        }
    }
    
    /**
     * 提取消息类型
     */
    private String extractMessageType(JsonNode messageNode) {
        try {
            JsonNode notifyData = messageNode.get("notify_data");
            if (notifyData != null) {
                JsonNode body = notifyData.get("body");
                if (body != null) {
                    JsonNode services = body.get("services");
                    if (services != null && services.isArray() && services.size() > 0) {
                        JsonNode firstService = services.get(0);
                        JsonNode properties = firstService.get("properties");
                        if (properties != null) {
                            return getTextValue(properties, "TYPE");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("提取消息类型时出现异常", e);
        }
        return null;
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
            // 如果已有监听器在运行，先停止它
            if (currentListenerId != null) {
                log.info("检测到已有监听器运行，ID: {}，先停止旧监听器", currentListenerId);
                huaweiIotAmqpUtil.stopListener(currentListenerId);
                currentListenerId = null;
                listenerStartTime = null;
            }
            
            log.info("启动IoT消息监听器...");
            CompletableFuture<String> future = huaweiIotAmqpUtil.startDefaultQueueListener(this::processRawMessage);
            
            future.thenAccept(listenerId -> {
                this.currentListenerId = listenerId;
                this.listenerStartTime = System.currentTimeMillis();
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
                listenerStartTime = null;
                log.info("IoT消息监听器已停止");
            }
        } catch (Exception e) {
            log.error("停止监听器异常", e);
        }
    }
    
    @Override
    public IoTListenerStatusResponse getListenerStatus() {
        // 创建消息统计
        long total = totalMessageCount.get();
        long success = successMessageCount.get();
        long failed = failedMessageCount.get();
        double successRate = total > 0 ? (double) success / total * 100 : 0;
        
        IoTListenerStatusResponse.MessageStats messageStats = new IoTListenerStatusResponse.MessageStats(
                total, success, failed, successRate
        );
        
        // 创建状态响应
        return new IoTListenerStatusResponse(
                currentListenerId != null, // 是否正在监听
                currentListenerId,         // 监听器ID
                listenerStartTime,         // 启动时间
                messageStats               // 消息统计
        );
    }
    
    @PreDestroy
    public void cleanup() {
        log.info("清理IoT消息监听器...");
        stopMessageListener();
        huaweiIotAmqpUtil.stopAllListeners();
    }
} 