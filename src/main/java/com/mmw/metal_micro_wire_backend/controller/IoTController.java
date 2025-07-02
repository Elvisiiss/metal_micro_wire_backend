package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.iot.IoTListenerStatusResponse;
import com.mmw.metal_micro_wire_backend.service.IoTMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * IoT监听器控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/iot")
@RequiredArgsConstructor
public class IoTController {
    
    private final IoTMessageService iotMessageService;
    
    /**
     * 开启IoT消息监听
     */
    @PostMapping("/start")
    public BaseResponse<String> startListener() {
        try {
            log.info("开启IoT消息监听...");
            iotMessageService.startMessageListener();
            return BaseResponse.success("IoT消息监听已开启");
        } catch (Exception e) {
            log.error("开启IoT消息监听失败", e);
            return BaseResponse.error("开启监听失败: " + e.getMessage());
        }
    }
    
    /**
     * 关闭IoT消息监听
     */
    @PostMapping("/stop")
    public BaseResponse<String> stopListener() {
        try {
            log.info("关闭IoT消息监听...");
            iotMessageService.stopMessageListener();
            return BaseResponse.success("IoT消息监听已关闭");
        } catch (Exception e) {
            log.error("关闭IoT消息监听失败", e);
            return BaseResponse.error("关闭监听失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取IoT监听器状态
     */
    @GetMapping("/status")
    public BaseResponse<IoTListenerStatusResponse> getListenerStatus() {
        try {
            log.info("获取IoT监听器状态...");
            IoTListenerStatusResponse status = iotMessageService.getListenerStatus();
            return BaseResponse.success(status);
        } catch (Exception e) {
            log.error("获取IoT监听器状态失败", e);
            return BaseResponse.error("获取状态失败: " + e.getMessage());
        }
    }
} 