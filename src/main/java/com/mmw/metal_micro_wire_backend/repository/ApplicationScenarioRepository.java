package com.mmw.metal_micro_wire_backend.repository;

import com.mmw.metal_micro_wire_backend.entity.ApplicationScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 应用场景数据访问层
 */
@Repository
public interface ApplicationScenarioRepository extends JpaRepository<ApplicationScenario, String>, JpaSpecificationExecutor<ApplicationScenario> {
    
    /**
     * 根据线材类型查找应用场景
     */
    List<ApplicationScenario> findByWireType(String wireType);
    
    /**
     * 根据应用场景名称查找应用场景
     */
    List<ApplicationScenario> findByScenarioNameContaining(String scenarioName);
    
    /**
     * 根据线材类型和应用场景名称查找应用场景
     */
    List<ApplicationScenario> findByWireTypeAndScenarioNameContaining(String wireType, String scenarioName);
} 