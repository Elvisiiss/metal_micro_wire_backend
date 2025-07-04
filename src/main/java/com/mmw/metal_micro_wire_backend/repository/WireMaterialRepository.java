package com.mmw.metal_micro_wire_backend.repository;

import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    /**
     * 根据最终评估结果查找线材数据
     */
    List<WireMaterial> findByFinalEvaluationResult(WireMaterial.FinalEvaluationResult finalEvaluationResult);
    
    /**
     * 根据多个最终评估结果查找线材数据
     * 用于获取需要人工处理的线材（未评估 + 待审核）
     */
    List<WireMaterial> findByFinalEvaluationResultIn(List<WireMaterial.FinalEvaluationResult> finalEvaluationResults);
    
    /**
     * 根据多个最终评估结果分页查找线材数据
     * 用于获取需要人工处理的线材（未评估 + 待审核）
     */
    Page<WireMaterial> findByFinalEvaluationResultIn(List<WireMaterial.FinalEvaluationResult> finalEvaluationResults, Pageable pageable);
} 