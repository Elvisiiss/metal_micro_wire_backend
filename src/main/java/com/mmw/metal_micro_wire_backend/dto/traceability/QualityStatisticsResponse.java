package com.mmw.metal_micro_wire_backend.dto.traceability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 质量统计响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityStatisticsResponse {
    
    /**
     * 统计维度名称（生产商名称、负责人姓名、工艺类型、生产机器等）
     */
    private String dimensionName;
    
    /**
     * 统计维度值（具体的生产商、负责人、工艺、机器名称）
     */
    private String dimensionValue;
    
    /**
     * 总批次数
     */
    private Long totalCount;
    
    /**
     * 合格批次数
     */
    private Long passCount;
    
    /**
     * 不合格批次数
     */
    private Long failCount;
    
    /**
     * 待审核批次数
     */
    private Long pendingReviewCount;
    
    /**
     * 未评估批次数
     */
    private Long unknownCount;
    
    /**
     * 合格率（百分比）
     */
    private BigDecimal passRate;
    
    /**
     * 不合格率（百分比）
     */
    private BigDecimal failRate;
    
    /**
     * 联系邮箱（用于问题通知）
     */
    private String contactEmail;
    
    /**
     * 计算合格率和不合格率
     */
    public void calculateRates() {
        if (totalCount == null || totalCount == 0) {
            this.passRate = BigDecimal.ZERO;
            this.failRate = BigDecimal.ZERO;
            return;
        }
        
        // 计算合格率
        if (passCount != null) {
            this.passRate = BigDecimal.valueOf(passCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
        } else {
            this.passRate = BigDecimal.ZERO;
        }
        
        // 计算不合格率
        if (failCount != null) {
            this.failRate = BigDecimal.valueOf(failCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
        } else {
            this.failRate = BigDecimal.ZERO;
        }
    }
    
    /**
     * 是否存在质量问题（不合格率超过阈值）
     */
    public boolean hasQualityIssue(BigDecimal threshold) {
        return failRate != null && failRate.compareTo(threshold) > 0;
    }
    
    /**
     * 获取问题严重程度描述
     */
    public String getIssueSeverity() {
        if (failRate == null) {
            return "无数据";
        }
        
        if (failRate.compareTo(BigDecimal.valueOf(20)) > 0) {
            return "严重";
        } else if (failRate.compareTo(BigDecimal.valueOf(10)) > 0) {
            return "中等";
        } else if (failRate.compareTo(BigDecimal.valueOf(5)) > 0) {
            return "轻微";
        } else {
            return "正常";
        }
    }
}
