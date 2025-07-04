package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.quality.CompletedEvaluationPageRequest;
import com.mmw.metal_micro_wire_backend.dto.quality.PendingReviewPageRequest;
import com.mmw.metal_micro_wire_backend.dto.quality.QualityEvaluationPageResponse;
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
     * 获取需要人工处理的线材列表
     * 包括未评估(UNKNOWN)和待人工审核(PENDING_REVIEW)状态的线材
     *
     * @return 需要人工处理的线材列表
     * @deprecated 使用分页版本 {@link #getPendingReviewMaterials(PendingReviewPageRequest)}
     */
    @Deprecated
    List<WireMaterial> getPendingReviewMaterials();
    
    /**
     * 分页获取需要人工处理的线材列表
     * 包括未评估(UNKNOWN)和待人工审核(PENDING_REVIEW)状态的线材
     *
     * @param request 分页请求参数
     * @return 分页响应
     */
    QualityEvaluationPageResponse getPendingReviewMaterials(PendingReviewPageRequest request);
    
    /**
     * 获取已完成评估的线材列表
     * 包括自动确定为合格(PASS)和不合格(FAIL)状态的线材，供人工重新审核
     *
     * @return 已完成评估的线材列表
     * @deprecated 使用分页版本 {@link #getCompletedMaterials(CompletedEvaluationPageRequest)}
     */
    @Deprecated
    List<WireMaterial> getCompletedMaterials();
    
    /**
     * 分页获取已完成评估的线材列表
     * 包括自动确定为合格(PASS)和不合格(FAIL)状态的线材，支持应用场景筛选和置信度排序
     *
     * @param request 分页请求参数
     * @return 分页响应
     */
    QualityEvaluationPageResponse getCompletedMaterials(CompletedEvaluationPageRequest request);
    
    /**
     * 人工审核确认最终结果
     * 支持对待审核状态和已完成状态的线材进行重新审核
     *
     * @param batchNumber 批次号
     * @param finalResult 最终评估结果
     * @param reviewRemark 审核备注
     * @return 是否成功
     */
    boolean confirmFinalResult(String batchNumber, WireMaterial.FinalEvaluationResult finalResult, String reviewRemark);
} 