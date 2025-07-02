package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.device.*;
import com.mmw.metal_micro_wire_backend.entity.Device;
import com.mmw.metal_micro_wire_backend.repository.DeviceRepository;
import com.mmw.metal_micro_wire_backend.service.DeviceService;
import com.mmw.metal_micro_wire_backend.util.HuaweiIotMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 设备服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final HuaweiIotMessageUtil huaweiIotMessageUtil;
    
    @Override
    public BaseResponse<DevicePageResponse> getDeviceList(DevicePageRequest request) {
        try {
            // 构建排序
            Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection()) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
            
            // 查询数据
            Page<Device> devicePage;
            if (request.getStatus() != null) {
                devicePage = deviceRepository.findByStatus(request.getStatus(), pageable);
            } else {
                devicePage = deviceRepository.findAll(pageable);
            }
            
            // 转换为响应DTO
            Page<DeviceResponse> responsePage = devicePage.map(DeviceResponse::fromEntity);
            DevicePageResponse pageResponse = DevicePageResponse.fromPage(responsePage);
            
            return BaseResponse.success(pageResponse);
        } catch (Exception e) {
            log.error("查询设备列表失败", e);
            return BaseResponse.error("查询设备列表失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<DeviceResponse> createDevice(CreateDeviceRequest request) {
        try {
            // 检查设备ID是否已存在
            if (deviceRepository.existsByDeviceId(request.getDeviceId())) {
                return BaseResponse.error("设备ID已存在：" + request.getDeviceId());
            }
            
            // 检查设备代码是否已存在（如果设备代码不为空）
            if (request.getDeviceCode() != null && !request.getDeviceCode().trim().isEmpty()) {
                Optional<Device> existingDevice = deviceRepository.findByDeviceCode(request.getDeviceCode());
                if (existingDevice.isPresent()) {
                    return BaseResponse.error("设备代码已存在：" + request.getDeviceCode());
                }
            }
            
            // 创建设备，初始状态固定为OFF
            Device device = Device.builder()
                    .deviceId(request.getDeviceId())
                    .deviceCode(request.getDeviceCode())
                    .status(Device.DeviceStatus.OFF)
                    .build();
            
            Device savedDevice = deviceRepository.save(device);
            log.info("创建设备成功，设备ID：{}，设备代码：{}，初始状态：OFF", 
                    savedDevice.getDeviceId(), savedDevice.getDeviceCode());
            
            return BaseResponse.success(DeviceResponse.fromEntity(savedDevice));
        } catch (Exception e) {
            log.error("创建设备失败", e);
            return BaseResponse.error("创建设备失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<Void> deleteDevice(String deviceId) {
        try {
            // 检查设备是否存在
            if (!deviceRepository.existsByDeviceId(deviceId)) {
                return BaseResponse.error("设备不存在：" + deviceId);
            }
            
            deviceRepository.deleteById(deviceId);
            log.info("删除设备成功，设备ID：{}", deviceId);
            
            return BaseResponse.success(null);
        } catch (Exception e) {
            log.error("删除设备失败，设备ID：{}", deviceId, e);
            return BaseResponse.error("删除设备失败：" + e.getMessage());
        }
    }
    
    @Override
    public BaseResponse<Void> controlDevice(DeviceControlRequest request) {
        try {
            // 检查设备是否存在
            Optional<Device> deviceOpt = deviceRepository.findById(request.getDeviceId());
            if (deviceOpt.isEmpty()) {
                return BaseResponse.error("设备不存在：" + request.getDeviceId());
            }
            
            Device device = deviceOpt.get();
            
            // 准备发送的控制消息
            String message = request.getTargetStatus() == Device.DeviceStatus.ON ? "ON" : "OFF";
            String messageName = "CMD_ON_OFF";
            String action = request.getTargetStatus() == Device.DeviceStatus.ON ? "启动" : "停止";
            
            log.info("开始{}设备，设备ID：{}，当前状态：{}，目标状态：{}", 
                    action, device.getDeviceId(), device.getStatus(), request.getTargetStatus());
            
            // 发送控制消息到华为云IoT设备
            boolean success = huaweiIotMessageUtil.sendMessage(
                    request.getDeviceId(), 
                    message, 
                    messageName
            );
            
            if (success) {
                log.info("设备控制消息发送成功：设备ID={}，消息={}，动作={}", 
                        request.getDeviceId(), message, action);
                return BaseResponse.<Void>success("控制消息已送达，请等待设备" + action + "完成", null);
            } else {
                log.error("设备控制消息发送失败：设备ID={}，消息={}，动作={}", 
                        request.getDeviceId(), message, action);
                return BaseResponse.error("设备控制消息发送失败");
            }
            
        } catch (Exception e) {
            log.error("控制设备失败", e);
            return BaseResponse.error("控制设备失败：" + e.getMessage());
        }
    }
    
    @Override
    public BaseResponse<DeviceResponse> getDeviceById(String deviceId) {
        try {
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            if (deviceOpt.isEmpty()) {
                return BaseResponse.error("设备不存在：" + deviceId);
            }
            
            return BaseResponse.success(DeviceResponse.fromEntity(deviceOpt.get()));
        } catch (Exception e) {
            log.error("查询设备失败，设备ID：{}", deviceId, e);
            return BaseResponse.error("查询设备失败：" + e.getMessage());
        }
    }
} 