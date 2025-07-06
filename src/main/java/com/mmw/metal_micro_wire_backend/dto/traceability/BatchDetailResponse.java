package com.mmw.metal_micro_wire_backend.dto.traceability;

import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 批次详情响应DTO
 * 用于溯源分析中展示具体的问题批次信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDetailResponse {
    
    /**
     * 批次卷序
     */
    private String batchNumber;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 生产商
     */
    private String manufacturer;
    
    /**
     * 负责人
     */
    private String responsiblePerson;
    
    /**
     * 工艺类型
     */
    private String processType;
    
    /**
     * 生产机器
     */
    private String productionMachine;
    
    /**
     * 联系邮箱
     */
    private String contactEmail;
    
    /**
     * 应用场景编号
     */
    private String scenarioCode;
    
    /**
     * 设备代码
     */
    private String deviceCode;
    
    /**
     * 规则引擎评估结果
     */
    private String evaluationResult;
    
    /**
     * 模型评估结果
     */
    private String modelEvaluationResult;
    
    /**
     * 模型置信度
     */
    private BigDecimal modelConfidence;
    
    /**
     * 最终评估结果
     */
    private String finalEvaluationResult;
    
    /**
     * 评估详情
     */
    private String evaluationMessage;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 质量指标
     */
    private QualityMetrics qualityMetrics;
    
    /**
     * 质量指标内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityMetrics {
        /**
         * 金属丝直径
         */
        private BigDecimal diameter;
        
        /**
         * 电导率
         */
        private BigDecimal resistance;
        
        /**
         * 延展率
         */
        private BigDecimal extensibility;
        
        /**
         * 重量
         */
        private BigDecimal weight;
    }
    
    /**
     * 从WireMaterial实体转换
     */
    public static BatchDetailResponse fromEntity(WireMaterial entity) {
        return BatchDetailResponse.builder()
                .batchNumber(entity.getBatchNumber())
                .deviceId(entity.getDeviceId())
                .manufacturer(entity.getManufacturer())
                .responsiblePerson(entity.getResponsiblePerson())
                .processType(entity.getProcessType())
                .productionMachine(entity.getProductionMachine())
                .contactEmail(entity.getContactEmail())
                .scenarioCode(entity.getScenarioCode())
                .deviceCode(entity.getDeviceCode())
                .evaluationResult(entity.getEvaluationResult() != null ? 
                        entity.getEvaluationResult().getDescription() : null)
                .modelEvaluationResult(entity.getModelEvaluationResult() != null ? 
                        entity.getModelEvaluationResult().getDescription() : null)
                .modelConfidence(entity.getModelConfidence())
                .finalEvaluationResult(entity.getFinalEvaluationResult() != null ? 
                        entity.getFinalEvaluationResult().getDescription() : null)
                .evaluationMessage(entity.getEvaluationMessage())
                .eventTime(entity.getEventTime())
                .createTime(entity.getCreateTime())
                .qualityMetrics(QualityMetrics.builder()
                        .diameter(entity.getDiameter())
                        .resistance(entity.getResistance())
                        .extensibility(entity.getExtensibility())
                        .weight(entity.getWeight())
                        .build())
                .build();
    }
    
    /**
     * 是否为问题批次
     */
    public boolean isProblematic() {
        return "不合格".equals(finalEvaluationResult);
    }
    
    /**
     * 是否需要人工审核
     */
    public boolean needsReview() {
        return "待人工审核".equals(finalEvaluationResult) || "未评估".equals(finalEvaluationResult);
    }
}
