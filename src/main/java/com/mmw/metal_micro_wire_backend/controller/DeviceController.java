package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.device.*;
import com.mmw.metal_micro_wire_backend.service.DeviceService;
import com.mmw.metal_micro_wire_backend.util.HuaweiIotMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 设备管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
@Validated
public class DeviceController {
    
    private final DeviceService deviceService;
    private final HuaweiIotMessageUtil huaweiIotMessageUtil;
    
    /**
     * 分页查询设备列表
     * 权限：已认证用户
     */
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<DevicePageResponse>> getDeviceList(
            @Valid DevicePageRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("用户{}查询设备列表，页码：{}，每页大小：{}，状态筛选：{}", 
                userId, request.getPage(), request.getSize(), request.getStatus());
        
        BaseResponse<DevicePageResponse> response = deviceService.getDeviceList(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据设备ID查询设备信息
     * 权限：已认证用户
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<BaseResponse<DeviceResponse>> getDeviceById(
            @PathVariable String deviceId,
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("用户{}查询设备信息，设备ID：{}", userId, deviceId);
        
        BaseResponse<DeviceResponse> response = deviceService.getDeviceById(deviceId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 创建设备
     * 权限：仅管理员用户（roleId=1）
     */
    @PostMapping
    public ResponseEntity<BaseResponse<DeviceResponse>> createDevice(
            @Valid @RequestBody CreateDeviceRequest request,
            HttpServletRequest httpRequest) {
        
        // 权限检查：仅管理员
        Integer roleId = (Integer) httpRequest.getAttribute("roleId");
        Long userId = (Long) httpRequest.getAttribute("userId");
        
        if (roleId == null || roleId != 1) {
            log.warn("用户{}尝试创建设备但权限不足，roleId：{}", userId, roleId);
            return ResponseEntity.ok(BaseResponse.error("权限不足，仅管理员可创建设备"));
        }
        
        log.info("管理员用户{}创建设备，设备ID：{}，设备代码：{}", 
                userId, request.getDeviceId(), request.getDeviceCode());
        
        BaseResponse<DeviceResponse> response = deviceService.createDevice(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除设备
     * 权限：仅管理员用户（roleId=1）
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<BaseResponse<Void>> deleteDevice(
            @PathVariable String deviceId,
            HttpServletRequest httpRequest) {
        
        // 权限检查：仅管理员
        Integer roleId = (Integer) httpRequest.getAttribute("roleId");
        Long userId = (Long) httpRequest.getAttribute("userId");
        
        if (roleId == null || roleId != 1) {
            log.warn("用户{}尝试删除设备但权限不足，roleId：{}", userId, roleId);
            return ResponseEntity.ok(BaseResponse.error("权限不足，仅管理员可删除设备"));
        }
        
        log.info("管理员用户{}删除设备，设备ID：{}", userId, deviceId);
        
        BaseResponse<Void> response = deviceService.deleteDevice(deviceId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 控制设备（启停）
     * 权限：仅管理员用户（roleId=1）
     * 
     * 说明：向设备发送控制命令消息，消息送达后返回成功。
     * 由于硬件限制，无法获得设备的实时响应，设备状态的实际更新
     * 通过AMQP消息监听异步完成，客户端需要轮询设备状态或监听状态变化。
     */
    @PostMapping("/control")
    public ResponseEntity<BaseResponse<Void>> controlDevice(
            @Valid @RequestBody DeviceControlRequest request,
            HttpServletRequest httpRequest) {
        
        // 权限检查：仅管理员
        Integer roleId = (Integer) httpRequest.getAttribute("roleId");
        Long userId = (Long) httpRequest.getAttribute("userId");
        
        if (roleId == null || roleId != 1) {
            log.warn("用户{}尝试控制设备但权限不足，roleId：{}", userId, roleId);
            return ResponseEntity.ok(BaseResponse.error("权限不足，仅管理员可控制设备"));
        }
        
        String action = request.getTargetStatus().name();
        log.info("管理员用户{}控制设备，设备ID：{}，目标状态：{}", 
                userId, request.getDeviceId(), action);
        
        BaseResponse<Void> response = deviceService.controlDevice(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 测试设备注册状态
     * 权限：仅管理员用户（roleId=1）
     * 
     * 说明：向指定设备发送测试连接消息（消息名：connect，内容：test）
     * 用于验证设备是否在华为云IoT平台上注册，而非设备在线状态
     * 华为云消息有缓存机制，消息能够送达即表示设备ID已在平台注册
     */
    @PostMapping("/test-connection")
    public ResponseEntity<BaseResponse<Void>> testConnection(
            @Valid @RequestBody TestConnectionRequest request,
            HttpServletRequest httpRequest) {
        
        // 权限检查：仅管理员
        Integer roleId = (Integer) httpRequest.getAttribute("roleId");
        Long userId = (Long) httpRequest.getAttribute("userId");
        
        if (roleId == null || roleId != 1) {
            log.warn("用户{}尝试测试设备注册状态但权限不足，roleId：{}", userId, roleId);
            return ResponseEntity.ok(BaseResponse.error("权限不足，仅管理员可测试设备注册状态"));
        }
        
        try {
            log.info("管理员用户{}测试设备注册状态，设备ID：{}", userId, request.getDeviceId());
            
            // 发送测试连接消息，验证设备注册状态
            boolean success = huaweiIotMessageUtil.sendMessage(
                    request.getDeviceId(), 
                    "test", 
                    "connect"
            );
            
            if (success) {
                log.info("设备注册状态测试消息发送成功：设备ID={}", request.getDeviceId());
                return ResponseEntity.ok(BaseResponse.<Void>success("测试消息已发送，设备已在华为云IoT平台注册", null));
            } else {
                log.error("设备注册状态测试消息发送失败：设备ID={}", request.getDeviceId());
                return ResponseEntity.ok(BaseResponse.error("测试消息发送失败，设备可能未在华为云IoT平台注册"));
            }
            
        } catch (Exception e) {
            log.error("测试设备注册状态失败：设备ID={}，错误={}", request.getDeviceId(), e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.error("测试失败：" + e.getMessage()));
        }
    }
} 