package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.iot.IoTListenerStatusResponse;

/**
 * IoT消息处理服务接口
 * 采用灵活的消息处理方式，避免固定DTO结构的限制
 */
public interface IoTMessageService {
    
    /**
     * 处理原始IoT消息
     * 这是主要的消息处理入口，接收原始JSON字符串
     * @param rawMessage 原始消息字符串
     */
    void processRawMessage(String rawMessage);
    
    /**
     * 启动消息监听
     */
    void startMessageListener();
    
    /**
     * 停止消息监听
     */
    void stopMessageListener();
    
    /**
     * 获取监听器状态
     * @return 监听器状态信息
     */
    IoTListenerStatusResponse getListenerStatus();
} 