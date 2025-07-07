package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.config.FileUploadConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件访问控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileUploadConfig fileUploadConfig;
    
    /**
     * 访问上传的文件
     * 
     * @param subPath 子路径（如：avatars）
     * @param filename 文件名
     * @return 文件资源
     */
    @GetMapping("/{subPath}/{filename:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String subPath,
            @PathVariable String filename) {
        
        try {
            // 构建文件路径
            Path filePath = Paths.get(fileUploadConfig.getUploadPath())
                    .resolve(subPath)
                    .resolve(filename)
                    .normalize();
            
            // 创建资源
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("文件不存在或不可读：{}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 确定内容类型
            String contentType = determineContentType(filename);
            
            log.debug("访问文件：{}，内容类型：{}", filePath, contentType);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("访问文件失败，子路径：{}，文件名：{}，错误：{}", subPath, filename, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 确定文件的内容类型
     */
    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
