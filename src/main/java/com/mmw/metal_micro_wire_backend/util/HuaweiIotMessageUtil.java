package com.mmw.metal_micro_wire_backend.util;

import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.auth.AbstractCredentials;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.core.region.Region;
import com.huaweicloud.sdk.iotda.v5.IoTDAClient;
import com.huaweicloud.sdk.iotda.v5.model.CreateMessageRequest;
import com.huaweicloud.sdk.iotda.v5.model.CreateMessageResponse;
import com.huaweicloud.sdk.iotda.v5.model.DeviceMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 华为云IoT消息推送工具类
 * 
 * 提供向华为云IoT设备发送消息的功能
 */
@Slf4j
@Component
public class HuaweiIotMessageUtil {

    @Value("${huawei.iot.ak}")
    private String ak;

    @Value("${huawei.iot.sk}")
    private String sk;

    @Value("${huawei.iot.endpoint}")
    private String iotdaEndpoint;

    @Value("${huawei.iot.project-id}")
    private String projectId;

    @Value("${huawei.iot.region:cn-north-4}")
    private String region;

    /**
     * 创建IoT客户端
     * 
     * @return IoTDAClient
     * @throws IllegalStateException 当配置不完整时抛出异常
     */
    private IoTDAClient createClient() {
        // 验证配置
        if (!StringUtils.hasText(ak) || !StringUtils.hasText(sk) || 
            !StringUtils.hasText(iotdaEndpoint) || !StringUtils.hasText(projectId)) {
            throw new IllegalStateException("华为云IoT配置不完整，请检查配置文件中的ak、sk、endpoint、project-id");
        }

        // 创建认证信息
        ICredential auth = new BasicCredentials()
                .withProjectId(projectId)
                // 标准版/企业版需要使用衍生算法，基础版请删除配置"withDerivedPredicate"
                .withDerivedPredicate(AbstractCredentials.DEFAULT_DERIVED_PREDICATE)
                .withAk(ak)
                .withSk(sk);

        // 创建IoT客户端
        return IoTDAClient.newBuilder()
                .withCredential(auth)
                .withRegion(new Region(region, iotdaEndpoint))
                .build();
    }

    /**
     * 发送消息到指定设备
     * 
     * @param deviceId 设备ID
     * @param message 消息内容
     * @param messageName 消息名称
     * @return 是否发送成功
     */
    public boolean sendMessage(String deviceId, String message, String messageName) {
        if (!StringUtils.hasText(deviceId)) {
            log.error("设备ID不能为空");
            return false;
        }
        
        if (!StringUtils.hasText(message)) {
            log.error("消息内容不能为空");
            return false;
        }

        try {
            IoTDAClient client = createClient();
            
            // 创建消息请求
            CreateMessageRequest request = new CreateMessageRequest();
            request.withDeviceId(deviceId);
            
            DeviceMessageRequest body = new DeviceMessageRequest();
            body.withMessage(message);
            body.withName(StringUtils.hasText(messageName) ? messageName : "defaultMessage");
            body.withMessageId(UUID.randomUUID().toString());
            request.withBody(body);

            // 发送消息
            CreateMessageResponse response = client.createMessage(request);
            log.info("消息发送成功: deviceId={}, messageId={}, messageName={}", 
                    deviceId, body.getMessageId(), messageName);
            log.debug("响应详情: {}", response.toString());
            
            return true;

        } catch (ConnectionException e) {
            log.error("连接异常: deviceId={}, error={}", deviceId, e.getMessage());
        } catch (RequestTimeoutException e) {
            log.error("请求超时: deviceId={}, error={}", deviceId, e.getMessage());
        } catch (ServiceResponseException e) {
            log.error("服务响应异常: deviceId={}, httpStatus={}, requestId={}, errorCode={}, errorMsg={}", 
                    deviceId, e.getHttpStatusCode(), e.getRequestId(), e.getErrorCode(), e.getErrorMsg());
        } catch (IllegalStateException e) {
            log.error("配置错误: {}", e.getMessage());
        } catch (Exception e) {
            log.error("发送消息失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
        }
        
        return false;
    }

    /**
     * 发送消息到指定设备（使用默认消息名称）
     * 
     * @param deviceId 设备ID
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(String deviceId, String message) {
        return sendMessage(deviceId, message, "defaultMessage");
    }

    /**
     * 异步发送消息到指定设备
     * 
     * @param deviceId 设备ID
     * @param message 消息内容
     * @param messageName 消息名称
     * @return CompletableFuture<Boolean>
     */
    public CompletableFuture<Boolean> sendMessageAsync(String deviceId, String message, String messageName) {
        return CompletableFuture.supplyAsync(() -> sendMessage(deviceId, message, messageName));
    }

    /**
     * 批量发送消息到多个设备
     * 
     * @param deviceIds 设备ID数组
     * @param message 消息内容
     * @param messageName 消息名称
     * @return 发送结果统计 [成功数, 失败数]
     */
    public int[] batchSendToDevices(String[] deviceIds, String message, String messageName) {
        if (deviceIds == null || deviceIds.length == 0) {
            log.warn("设备ID列表为空");
            return new int[]{0, 0};
        }

        int successCount = 0;
        int failCount = 0;

        for (String deviceId : deviceIds) {
            if (sendMessage(deviceId, message, messageName)) {
                successCount++;
            } else {
                failCount++;
            }
        }

        log.info("批量发送完成: 总数={}, 成功={}, 失败={}", deviceIds.length, successCount, failCount);
        return new int[]{successCount, failCount};
    }

    /**
     * 批量发送消息到单个设备（发送多条消息）
     * 
     * @param deviceId 设备ID
     * @param messages 消息内容数组
     * @param messageName 消息名称
     * @param intervalMs 发送间隔（毫秒）
     * @return 发送结果统计 [成功数, 失败数]
     */
    public int[] batchSendToDevice(String deviceId, String[] messages, String messageName, long intervalMs) {
        if (messages == null || messages.length == 0) {
            log.warn("消息列表为空");
            return new int[]{0, 0};
        }

        int successCount = 0;
        int failCount = 0;

        log.info("开始批量发送消息: deviceId={}, 消息数量={}, 间隔={}ms", deviceId, messages.length, intervalMs);

        for (int i = 0; i < messages.length; i++) {
            if (sendMessage(deviceId, messages[i], messageName)) {
                successCount++;
            } else {
                failCount++;
            }

            // 间隔等待（最后一条消息不需要等待）
            if (i < messages.length - 1 && intervalMs > 0) {
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    log.warn("等待被中断", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("批量发送完成: deviceId={}, 总数={}, 成功={}, 失败={}", 
                deviceId, messages.length, successCount, failCount);
        return new int[]{successCount, failCount};
    }

    /**
     * 异步批量发送消息到多个设备
     * 
     * @param deviceIds 设备ID数组
     * @param message 消息内容
     * @param messageName 消息名称
     * @param timeoutSeconds 超时时间（秒）
     * @return CompletableFuture<int[]> [成功数, 失败数]
     */
    public CompletableFuture<int[]> batchSendToDevicesAsync(String[] deviceIds, String message, String messageName, int timeoutSeconds) {
        return CompletableFuture.supplyAsync(() -> batchSendToDevices(deviceIds, message, messageName))
                .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    log.error("异步批量发送超时或失败: {}", throwable.getMessage());
                    return new int[]{0, deviceIds != null ? deviceIds.length : 0};
                });
    }

    /**
     * 检查IoT服务配置是否完整
     * 
     * @return 配置是否完整
     */
    public boolean isConfigurationValid() {
        return StringUtils.hasText(ak) && 
               StringUtils.hasText(sk) && 
               StringUtils.hasText(iotdaEndpoint) && 
               StringUtils.hasText(projectId);
    }

    /**
     * 获取配置状态信息
     * 
     * @return 配置状态描述
     */
    public String getConfigurationStatus() {
        StringBuilder status = new StringBuilder("华为云IoT配置状态:\n");
        status.append("AK: ").append(StringUtils.hasText(ak) ? "已配置" : "未配置").append("\n");
        status.append("SK: ").append(StringUtils.hasText(sk) ? "已配置" : "未配置").append("\n");
        status.append("Endpoint: ").append(StringUtils.hasText(iotdaEndpoint) ? iotdaEndpoint : "未配置").append("\n");
        status.append("Project ID: ").append(StringUtils.hasText(projectId) ? projectId : "未配置").append("\n");
        status.append("Region: ").append(region).append("\n");
        status.append("配置完整性: ").append(isConfigurationValid() ? "完整" : "不完整");
        return status.toString();
    }
} 