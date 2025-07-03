package com.mmw.metal_micro_wire_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 线材实体类 - 存储金属微丝检测数据
 */
@Entity
@Table(name = "wire_materials")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WireMaterial {
    
    /**
     * 批次卷序 - 主键
     */
    @Id
    @Column(name = "batch_number", nullable = false)
    private String batchNumber;
    
    /**
     * 设备ID
     */
    @Column(name = "device_id", nullable = false)
    private String deviceId;
    
    /**
     * 金属丝直径
     */
    @Column(name = "diameter", precision = 10, scale = 2)
    private BigDecimal diameter;
    
    /**
     * 电导率
     */
    @Column(name = "resistance", precision = 10, scale = 2)
    private BigDecimal resistance;
    
    /**
     * 延展率
     */
    @Column(name = "extensibility", precision = 10, scale = 2)
    private BigDecimal extensibility;
    
    /**
     * 重量
     */
    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;
    
    /**
     * 原始生产信息（十六进制GBK编码）
     */
    @Column(name = "source_origin_raw", columnDefinition = "TEXT")
    private String sourceOriginRaw;
    
    /**
     * 生产商
     */
    @Column(name = "manufacturer")
    private String manufacturer;
    
    /**
     * 负责人
     */
    @Column(name = "responsible_person")
    private String responsiblePerson;
    
    /**
     * 工艺类型
     */
    @Column(name = "process_type")
    private String processType;
    
    /**
     * 生产机器
     */
    @Column(name = "production_machine")
    private String productionMachine;
    
    /**
     * 联系方式（邮箱）
     */
    @Column(name = "contact_email")
    private String contactEmail;
    
    /**
     * 应用场景编号（从批次号解析得出）
     */
    @Column(name = "scenario_code", length = 2)
    private String scenarioCode;
    
    /**
     * 设备代码（从批次号解析得出，对应批次号13-14位）
     */
    @Column(name = "device_code", length = 2)
    private String deviceCode;
    
    /**
     * 事件发生时间
     */
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;
    
    /**
     * 规则引擎评估结果
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_result", length = 10)
    @Builder.Default
    private EvaluationResult evaluationResult = EvaluationResult.UNKNOWN;
    
    /**
     * 评估详情（记录不合格的具体指标）
     */
    @Column(name = "evaluation_message", length = 500)
    private String evaluationMessage;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 评估结果枚举
     */
    public enum EvaluationResult {
        PASS("合格"),
        FAIL("不合格"), 
        UNKNOWN("未评估");
        
        private final String description;
        
        EvaluationResult(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
} 