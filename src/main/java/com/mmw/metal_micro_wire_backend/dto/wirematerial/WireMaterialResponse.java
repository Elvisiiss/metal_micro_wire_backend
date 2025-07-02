package com.mmw.metal_micro_wire_backend.dto.wirematerial;

import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 线材信息响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WireMaterialResponse {
    
    /**
     * 批次卷序
     */
    private String batchNumber;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
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
    
    /**
     * 原始生产信息（十六进制GBK编码）
     */
    private String sourceOriginRaw;
    
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
     * 联系方式（邮箱）
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
     * 事件发生时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 从实体转换为响应DTO
     */
    public static WireMaterialResponse fromEntity(WireMaterial entity) {
        return WireMaterialResponse.builder()
                .batchNumber(entity.getBatchNumber())
                .deviceId(entity.getDeviceId())
                .diameter(entity.getDiameter())
                .resistance(entity.getResistance())
                .extensibility(entity.getExtensibility())
                .weight(entity.getWeight())
                .sourceOriginRaw(entity.getSourceOriginRaw())
                .manufacturer(entity.getManufacturer())
                .responsiblePerson(entity.getResponsiblePerson())
                .processType(entity.getProcessType())
                .productionMachine(entity.getProductionMachine())
                .contactEmail(entity.getContactEmail())
                .scenarioCode(entity.getScenarioCode())
                .deviceCode(entity.getDeviceCode())
                .eventTime(entity.getEventTime())
                .createTime(entity.getCreateTime())
                .build();
    }
} 