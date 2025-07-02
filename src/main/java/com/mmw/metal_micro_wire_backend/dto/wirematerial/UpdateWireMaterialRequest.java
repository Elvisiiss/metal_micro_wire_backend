package com.mmw.metal_micro_wire_backend.dto.wirematerial;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
// import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 线材更新请求DTO
 */
@Data
public class UpdateWireMaterialRequest {
    
    /**
     * 金属丝直径
     */
    @DecimalMin(value = "0.0", message = "直径不能为负数")
    private BigDecimal diameter;
    
    /**
     * 电导率
     */
    @DecimalMin(value = "0.0", message = "电导率不能为负数")
    private BigDecimal resistance;
    
    /**
     * 延展率
     */
    @DecimalMin(value = "0.0", message = "延展率不能为负数")
    private BigDecimal extensibility;
    
    /**
     * 重量
     */
    @DecimalMin(value = "0.0", message = "重量不能为负数")
    private BigDecimal weight;
    
    /**
     * 生产商
     */
    @Size(max = 100, message = "生产商名称长度不能超过100个字符")
    private String manufacturer;
    
    /**
     * 负责人
     */
    @Size(max = 50, message = "负责人姓名长度不能超过50个字符")
    private String responsiblePerson;
    
    /**
     * 工艺类型
     */
    @Size(max = 50, message = "工艺类型长度不能超过50个字符")
    private String processType;
    
    /**
     * 生产机器
     */
    @Size(max = 100, message = "生产机器名称长度不能超过100个字符")
    private String productionMachine;
    
    /**
     * 联系方式（邮箱）
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String contactEmail;
} 