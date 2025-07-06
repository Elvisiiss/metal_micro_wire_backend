package com.mmw.metal_micro_wire_backend.dto.traceability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 质量问题响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityIssueResponse {
    
    /**
     * 问题ID（自动生成）
     */
    private String issueId;
    
    /**
     * 问题维度（生产商、负责人、工艺类型、生产机器）
     */
    private String dimension;
    
    /**
     * 问题维度值
     */
    private String dimensionValue;
    
    /**
     * 问题严重程度
     */
    private IssueSeverity severity;
    
    /**
     * 不合格率
     */
    private BigDecimal failRate;
    
    /**
     * 不合格批次数
     */
    private Long failCount;
    
    /**
     * 总批次数
     */
    private Long totalCount;
    
    /**
     * 问题描述
     */
    private String description;
    
    /**
     * 建议措施
     */
    private String recommendation;
    
    /**
     * 联系邮箱
     */
    private String contactEmail;
    
    /**
     * 相关批次号列表（最近的问题批次）
     */
    private List<String> relatedBatchNumbers;
    
    /**
     * 问题发现时间
     */
    private LocalDateTime discoveredTime;
    
    /**
     * 是否已通知
     */
    @Builder.Default
    private Boolean notified = false;
    
    /**
     * 通知时间
     */
    private LocalDateTime notifiedTime;
    
    /**
     * 问题严重程度枚举
     */
    public enum IssueSeverity {
        CRITICAL("严重", "不合格率超过20%，需要立即处理"),
        HIGH("高", "不合格率超过10%，需要优先处理"),
        MEDIUM("中等", "不合格率超过5%，需要关注"),
        LOW("低", "不合格率较低，建议监控");
        
        private final String level;
        private final String description;
        
        IssueSeverity(String level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public String getLevel() {
            return level;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * 根据不合格率确定严重程度
         */
        public static IssueSeverity fromFailRate(BigDecimal failRate) {
            if (failRate == null) {
                return LOW;
            }
            
            if (failRate.compareTo(BigDecimal.valueOf(20)) >= 0) {
                return CRITICAL;
            } else if (failRate.compareTo(BigDecimal.valueOf(10)) >= 0) {
                return HIGH;
            } else if (failRate.compareTo(BigDecimal.valueOf(5)) >= 0) {
                return MEDIUM;
            } else {
                return LOW;
            }
        }
    }
    
    /**
     * 生成问题描述
     */
    public void generateDescription() {
        this.description = String.format("%s【%s】存在质量问题，不合格率为%.2f%%（%d/%d），严重程度：%s", 
                dimension, dimensionValue, failRate, failCount, totalCount, severity.getLevel());
    }
    
    /**
     * 生成建议措施
     */
    public void generateRecommendation() {
        switch (severity) {
            case CRITICAL:
                this.recommendation = "立即停止相关生产活动，进行全面质量检查，分析根本原因并制定整改措施";
                break;
            case HIGH:
                this.recommendation = "优先处理该问题，加强质量控制，增加检测频次，分析问题原因";
                break;
            case MEDIUM:
                this.recommendation = "关注该问题趋势，适当增加质量检查，分析是否存在系统性问题";
                break;
            case LOW:
                this.recommendation = "持续监控质量状况，定期回顾，预防问题扩大";
                break;
        }
    }
}
