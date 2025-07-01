package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.device.*;
import com.mmw.metal_micro_wire_backend.service.DeviceService;
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
    
    /**
     * 分页查询设备列表
     * 权限：已认证用户
     */
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<DevicePageResponse>> getDeviceList(
            @Valid DevicePageRequest request,
            HttpServletRequest httpRequest) {
        
        Integer userId = (Integer) httpRequest.getAttribute("userId");
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
        
        Integer userId = (Integer) httpRequest.getAttribute("userId");
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
        Integer userId = (Integer) httpRequest.getAttribute("userId");
        
        if (roleId == null || roleId != 1) {
            log.warn("用户{}尝试创建设备但权限不足，roleId：{}", userId, roleId);
            return ResponseEntity.ok(BaseResponse.error("权限不足，仅管理员可创建设备"));
        }
        
        log.info("管理员用户{}创建设备，设备ID：{}", userId, request.getDeviceId());
        
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
        Integer userId = (Integer) httpRequest.getAttribute("userId");
        
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
     * 注意：暂时只打印日志，不操作数据库
     */
    @PostMapping("/control")
    public ResponseEntity<BaseResponse<Void>> controlDevice(
            @Valid @RequestBody DeviceControlRequest request,
            HttpServletRequest httpRequest) {
        
        // 权限检查：仅管理员
        Integer roleId = (Integer) httpRequest.getAttribute("roleId");
        Integer userId = (Integer) httpRequest.getAttribute("userId");
        
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
} 