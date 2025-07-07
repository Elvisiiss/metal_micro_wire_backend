package com.mmw.metal_micro_wire_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户名修改请求DTO
 */
@Data
public class UpdateUsernameRequest {
    
    /**
     * 新用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 20, message = "用户名长度必须在1-20个字符之间")
    @Pattern(regexp = "^[^@]*$", message = "用户名不能包含@符号")
    private String newUsername;
}
