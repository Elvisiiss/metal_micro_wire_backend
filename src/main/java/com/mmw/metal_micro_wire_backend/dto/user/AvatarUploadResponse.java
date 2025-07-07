package com.mmw.metal_micro_wire_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 头像上传响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUploadResponse {
    
    /**
     * 头像访问URL
     */
    private String avatarUrl;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 原始文件名
     */
    private String originalFilename;
    
    /**
     * 上传时间戳
     */
    private Long uploadTimestamp;
}
