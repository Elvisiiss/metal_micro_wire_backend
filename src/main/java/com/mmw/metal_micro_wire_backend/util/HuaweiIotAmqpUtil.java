package com.mmw.metal_micro_wire_backend.util;

import com.iot.amqp.AmqpClient;
import com.iot.amqp.AmqpClientOptions;
import com.mmw.metal_micro_wire_backend.config.HuaweiIotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.MessageConsumer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 华为云IoT AMQP工具类
 */
@Slf4j
@Component
public class HuaweiIotAmqpUtil {
    
    @Autowired
    private HuaweiIotConfig huaweiIotConfig;
    
    // 客户端管理
    private final ConcurrentHashMap<String, AmqpClient> clients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MessageConsumer> consumers = new ConcurrentHashMap<>();
    
    /**
     * 创建AMQP客户端配置
     */
    private AmqpClientOptions createOptions() {
        HuaweiIotConfig.AmqpConfig amqpConfig = huaweiIotConfig.getAmqp();
        return AmqpClientOptions.builder()
                .host(amqpConfig.getHost())
                .port(amqpConfig.getPort())
                .accessKey(amqpConfig.getAccessKey())
                .accessCode(amqpConfig.getAccessCode())
                .queuePrefetch(amqpConfig.getQueuePrefetch())
                .isAutoAcknowledge(amqpConfig.isAutoAcknowledge())
                .reconnectDelay(amqpConfig.getReconnectDelay())
                .maxReconnectDelay(amqpConfig.getMaxReconnectDelay())
                .build();
    }
    
    /**
     * 启动默认队列监听器
     */
    public CompletableFuture<String> startDefaultQueueListener(Consumer<String> messageHandler) {
        return startQueueListener(huaweiIotConfig.getAmqp().getDefaultQueue(), messageHandler);
    }
    
    /**
     * 启动指定队列监听器
     */
    public CompletableFuture<String> startQueueListener(String queueName, Consumer<String> messageHandler) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String clientId = queueName + "_" + System.currentTimeMillis();
                
                AmqpClientOptions options = createOptions();
                AmqpClient amqpClient = new AmqpClient(options);
                amqpClient.initialize();
                
                MessageConsumer consumer = amqpClient.newConsumer(queueName);
                consumer.setMessageListener(message -> {
                    try {
                        String messageBody = message.getBody(String.class);
                        log.info("收到IoT消息，队列: {}", queueName);
                        
                        if (messageHandler != null) {
                            messageHandler.accept(messageBody);
                        }
                    } catch (Exception e) {
                        log.error("处理IoT消息失败", e);
                    }
                });
                
                clients.put(clientId, amqpClient);
                consumers.put(clientId, consumer);
                
                log.info("IoT监听器启动成功，队列: {}, ID: {}", queueName, clientId);
                return clientId;
                
            } catch (Exception e) {
                log.error("启动IoT监听器失败", e);
                throw new RuntimeException("启动IoT监听器失败", e);
            }
        });
    }
    
    /**
     * 停止指定监听器
     */
    public void stopListener(String clientId) {
        try {
            MessageConsumer consumer = consumers.remove(clientId);
            if (consumer != null) {
                consumer.close();
            }
            
            AmqpClient client = clients.remove(clientId);
            if (client != null) {
                client.close();
                log.info("已停止监听器: {}", clientId);
            }
        } catch (Exception e) {
            log.error("停止监听器失败: {}", clientId, e);
        }
    }
    
    /**
     * 停止所有监听器
     */
    public void stopAllListeners() {
        log.info("停止所有IoT监听器...");
        
        consumers.forEach((clientId, consumer) -> {
            try {
                consumer.close();
            } catch (Exception e) {
                log.error("关闭消费者失败: {}", clientId, e);
            }
        });
        consumers.clear();
        
        clients.forEach((clientId, client) -> {
            try {
                client.close();
            } catch (Exception e) {
                log.error("关闭客户端失败: {}", clientId, e);
            }
        });
        clients.clear();
        
        log.info("所有IoT监听器已停止");
    }
    
    /**
     * 测试连接
     */
    public boolean testConnection() {
        try {
            AmqpClientOptions options = createOptions();
            AmqpClient testClient = new AmqpClient(options);
            testClient.initialize();
            testClient.close();
            log.info("IoT连接测试成功");
            return true;
        } catch (Exception e) {
            log.error("IoT连接测试失败", e);
            return false;
        }
    }
} 