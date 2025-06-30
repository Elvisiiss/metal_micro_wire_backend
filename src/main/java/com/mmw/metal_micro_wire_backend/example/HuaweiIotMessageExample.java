package com.mmw.metal_micro_wire_backend.example;

import com.mmw.metal_micro_wire_backend.util.HuaweiIotMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 华为云IoT消息推送示例
 * 
 * 通过配置 example.huawei-iot.enabled=true 来启用此示例
 * 
 * 使用方法：
 * 1. 确保配置文件中华为云IoT相关配置正确
 * 2. 在application.yml中添加: example.huawei-iot.enabled=true
 * 3. 启动应用程序即可看到示例运行
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "example.huawei-iot.enabled", havingValue = "true")
public class HuaweiIotMessageExample implements CommandLineRunner {

    private final HuaweiIotMessageUtil huaweiIotMessageUtil;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== 华为云IoT消息推送示例开始 ===");
        
        // 检查配置
        if (!huaweiIotMessageUtil.isConfigurationValid()) {
            log.error("华为云IoT配置不完整，请检查配置文件");
            log.info(huaweiIotMessageUtil.getConfigurationStatus());
            return;
        }
        
        log.info("配置检查通过");
        log.info(huaweiIotMessageUtil.getConfigurationStatus());
        
        // 示例设备ID（请替换为实际的设备ID）
        String deviceId = "6857a366d582f2001833e1e6_8888";
        
        // 1. 单条消息发送示例
        singleMessageExample(deviceId);
        
        // 2. 批量消息发送示例
        batchMessageExample(deviceId);
        
        // 3. 异步消息发送示例
        asyncMessageExample(deviceId);
        
        log.info("=== 华为云IoT消息推送示例结束 ===");
    }
    
    /**
     * 单条消息发送示例
     */
    private void singleMessageExample(String deviceId) {
        log.info("--- 单条消息发送示例 ---");
        
        // 发送简单消息
        boolean result1 = huaweiIotMessageUtil.sendMessage(deviceId, "Hello World!");
        log.info("发送结果1: {}", result1 ? "成功" : "失败");
        
        // 发送带消息名称的消息
        boolean result2 = huaweiIotMessageUtil.sendMessage(deviceId, "测试消息内容", "testMessage");
        log.info("发送结果2: {}", result2 ? "成功" : "失败");
        
        // 等待一段时间
        sleep(2000);
    }
    
    /**
     * 批量消息发送示例
     */
    private void batchMessageExample(String deviceId) {
        log.info("--- 批量消息发送示例 ---");
        
        // 向多个设备发送相同消息
        String[] deviceIds = {deviceId}; // 可以添加更多设备ID
        int[] result1 = huaweiIotMessageUtil.batchSendToDevices(deviceIds, "批量消息测试", "batchMessage");
        log.info("批量发送结果: 成功={}, 失败={}", result1[0], result1[1]);
        
        // 向单个设备发送多条消息
        String[] messages = {"消息1", "消息2", "消息3"};
        int[] result2 = huaweiIotMessageUtil.batchSendToDevice(deviceId, messages, "multiMessage", 1000);
        log.info("多条消息发送结果: 成功={}, 失败={}", result2[0], result2[1]);
    }
    
    /**
     * 异步消息发送示例
     */
    private void asyncMessageExample(String deviceId) {
        log.info("--- 异步消息发送示例 ---");
        
        // 异步发送单条消息
        huaweiIotMessageUtil.sendMessageAsync(deviceId, "异步消息", "asyncMessage")
                .thenAccept(result -> log.info("异步发送结果: {}", result ? "成功" : "失败"))
                .exceptionally(throwable -> {
                    log.error("异步发送异常: {}", throwable.getMessage());
                    return null;
                });
        
        // 异步批量发送
        String[] deviceIds = {deviceId};
        huaweiIotMessageUtil.batchSendToDevicesAsync(deviceIds, "异步批量消息", "asyncBatchMessage", 30)
                .thenAccept(result -> log.info("异步批量发送结果: 成功={}, 失败={}", result[0], result[1]))
                .exceptionally(throwable -> {
                    log.error("异步批量发送异常: {}", throwable.getMessage());
                    return null;
                });
        
        // 等待异步操作完成
        sleep(5000);
    }
    
    /**
     * 线程休眠工具方法
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("休眠被中断", e);
        }
    }
} 