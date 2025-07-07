package com.mmw.metal_micro_wire_backend.util;

import com.mmw.metal_micro_wire_backend.config.FileUploadConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadUtil {
    
    private final FileUploadConfig fileUploadConfig;
    
    /**
     * 上传头像文件
     * 
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 文件访问URL
     * @throws IOException 文件操作异常
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        // 验证文件
        validateFile(file);
        
        // 创建上传目录
        String uploadDir = fileUploadConfig.getAvatarUploadPath();
        log.info("头像上传目录：{}", uploadDir);
        createDirectoryIfNotExists(uploadDir);

        // 生成文件名
        String fileName = generateFileName(file.getOriginalFilename(), userId);

        // 保存文件
        Path filePath = Paths.get(uploadDir, fileName);
        log.info("文件保存路径：{}", filePath.toAbsolutePath());
        file.transferTo(filePath.toFile());
        
        // 生成访问URL
        String fileUrl = generateFileUrl(fileUploadConfig.getAvatarPath(), fileName);
        
        log.info("头像上传成功，用户ID：{}，文件名：{}，文件大小：{} bytes", 
                userId, fileName, file.getSize());
        
        return fileUrl;
    }
    
    /**
     * 删除头像文件
     * 
     * @param avatarUrl 头像URL
     * @return 是否删除成功
     */
    public boolean deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return true;
        }
        
        try {
            // 从URL中提取文件路径
            String filePath = extractFilePathFromUrl(avatarUrl);
            if (filePath == null) {
                log.warn("无法从URL中提取文件路径：{}", avatarUrl);
                return false;
            }
            
            // 删除文件
            Path path = Paths.get(fileUploadConfig.getAbsoluteUploadPath(), filePath);
            boolean deleted = Files.deleteIfExists(path);
            
            if (deleted) {
                log.info("头像文件删除成功：{}", filePath);
            } else {
                log.warn("头像文件不存在或删除失败：{}", filePath);
            }
            
            return deleted;
            
        } catch (Exception e) {
            log.error("删除头像文件失败，URL：{}，错误：{}", avatarUrl, e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (file.getSize() > fileUploadConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("文件大小超过限制，最大允许 " + 
                    (fileUploadConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }
        
        String contentType = file.getContentType();
        if (!fileUploadConfig.isAllowedImageType(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型：" + contentType);
        }
        
        String originalFilename = file.getOriginalFilename();
        if (!fileUploadConfig.isAllowedExtension(originalFilename)) {
            throw new IllegalArgumentException("不支持的文件扩展名");
        }
    }
    
    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.info("创建上传目录：{}", dirPath);
        }
    }
    
    /**
     * 生成文件名
     */
    private String generateFileName(String originalFilename, Long userId) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        
        return String.format("avatar_%d_%s_%s%s", userId, timestamp, uuid, extension);
    }
    
    /**
     * 生成文件访问URL
     */
    private String generateFileUrl(String subPath, String fileName) {
        return fileUploadConfig.getUrlPrefix() + "/" + subPath + "/" + fileName;
    }
    
    /**
     * 从URL中提取文件路径
     */
    private String extractFilePathFromUrl(String url) {
        String prefix = fileUploadConfig.getUrlPrefix() + "/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return null;
    }
}
