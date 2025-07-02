package com.mmw.metal_micro_wire_backend.repository;

import com.mmw.metal_micro_wire_backend.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 设备数据访问层
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    
    /**
     * 根据状态分页查询设备
     */
    Page<Device> findByStatus(Device.DeviceStatus status, Pageable pageable);
    
    /**
     * 查询所有设备（分页）
     */
    @Override
    Page<Device> findAll(Pageable pageable);
    
    /**
     * 检查设备ID是否存在
     */
    boolean existsByDeviceId(String deviceId);
    
    /**
     * 根据设备代码查找设备
     */
    java.util.Optional<Device> findByDeviceCode(String deviceCode);
} 