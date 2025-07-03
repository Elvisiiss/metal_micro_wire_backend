package com.mmw.metal_micro_wire_backend.repository;

import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 线材数据访问层
 */
@Repository
public interface WireMaterialRepository extends JpaRepository<WireMaterial, String>, JpaSpecificationExecutor<WireMaterial> {
    
    /**
     * 根据应用场景编号查找线材数据
     */
    List<WireMaterial> findByScenarioCode(String scenarioCode);
} 