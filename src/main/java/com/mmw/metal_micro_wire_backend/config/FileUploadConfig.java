package com.mmw.metal_micro_wire_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.file-upload")
public class FileUploadConfig {
    
    /**
     * 文件上传根目录
     */
    private String uploadPath = "uploads";
    
    /**
     * 头像上传子目录
     */
    private String avatarPath = "avatars";
    
    /**
     * 最大文件大小（字节）- 默认2MB
     */
    private long maxFileSize = 2 * 1024 * 1024;
    
    /**
     * 允许的图片文件类型
     */
    private List<String> allowedImageTypes = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    /**
     * 允许的文件扩展名
     */
    private List<String> allowedExtensions = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );
    
    /**
     * 文件访问URL前缀
     */
    private String urlPrefix = "/api/files";
    
    /**
     * 获取完整的头像上传路径（绝对路径）
     */
    public String getAvatarUploadPath() {
        // 获取项目根目录的绝对路径
        String projectRoot = System.getProperty("user.dir");
        return projectRoot + File.separator + uploadPath + File.separator + avatarPath;
    }

    /**
     * 获取上传根目录的绝对路径
     */
    public String getAbsoluteUploadPath() {
        String projectRoot = System.getProperty("user.dir");
        return projectRoot + File.separator + uploadPath;
    }
    
    /**
     * 检查文件类型是否允许
     */
    public boolean isAllowedImageType(String contentType) {
        return contentType != null && allowedImageTypes.contains(contentType.toLowerCase());
    }
    
    /**
     * 检查文件扩展名是否允许
     */
    public boolean isAllowedExtension(String filename) {
        if (filename == null) {
            return false;
        }
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        return allowedExtensions.contains(extension);
    }
}
