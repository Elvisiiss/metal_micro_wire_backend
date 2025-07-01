package com.mmw.metal_micro_wire_backend.repository;

import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 线材数据访问层
 */
@Repository
public interface WireMaterialRepository extends JpaRepository<WireMaterial, String> {
    
} 