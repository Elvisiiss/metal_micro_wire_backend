package com.mmw.metal_micro_wire_backend.dto.traceability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 自定义邮件通知请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomNotificationRequest {

    @NotEmpty(message = "收件人列表不能为空")
    private List<String> recipients;

    @NotBlank(message = "邮件主题不能为空")
    private String subject;

    @NotBlank(message = "邮件内容不能为空")
    private String content;

    private String emailType;

    private String additionalData;

    @Builder.Default
    private Boolean isHtml = false;
}
