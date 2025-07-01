package com.mmw.metal_micro_wire_backend.repository;

import com.mmw.metal_micro_wire_backend.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 设备数据访问层
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    
} 