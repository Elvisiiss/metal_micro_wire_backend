package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.entity.WireMaterial;

import java.util.List;

/**
 * 质量评估服务接口
 * 整合规则引擎和机器学习模型的评估结果
 */
public interface QualityEvaluationService {
    
    /**
     * 综合评估单个线材质量
     * 结合规则引擎和机器学习模型的评估结果
     *
     * @param wireMaterial 线材实体
     * @return 更新后的线材实体
     */
    WireMaterial evaluateWireMaterial(WireMaterial wireMaterial);
    
    /**
     * 批量评估线材质量
     *
     * @param wireMaterials 线材实体列表
     * @return 更新后的线材实体列表
     */
    List<WireMaterial> evaluateBatch(List<WireMaterial> wireMaterials);
    
    /**
     * 重新评估指定场景的所有线材
     *
     * @param scenarioCode 应用场景代码
     * @return 重新评估的线材数量
     */
    int reEvaluateByScenario(String scenarioCode);
    
    /**
     * 获取需要人工审核的线材列表
     *
     * @return 需要人工审核的线材列表
     */
    List<WireMaterial> getPendingReviewMaterials();
    
    /**
     * 人工审核确认最终结果
     *
     * @param batchNumber 批次号
     * @param finalResult 最终评估结果
     * @param reviewRemark 审核备注
     * @return 是否成功
     */
    boolean confirmFinalResult(String batchNumber, WireMaterial.FinalEvaluationResult finalResult, String reviewRemark);
} 