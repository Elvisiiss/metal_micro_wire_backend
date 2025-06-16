package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class HealthController {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<BaseResponse<Map<String, String>>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        
        // 检查数据库连接
        try {
            Connection connection = dataSource.getConnection();
            connection.close();
            status.put("database", "connected");
        } catch (Exception e) {
            status.put("database", "disconnected: " + e.getMessage());
        }
        
        // 检查Redis连接
        try {
            redisTemplate.opsForValue().set("health_check", "test");
            String value = redisTemplate.opsForValue().get("health_check");
            if ("test".equals(value)) {
                status.put("redis", "connected");
                redisTemplate.delete("health_check");
            } else {
                status.put("redis", "test failed");
            }
        } catch (Exception e) {
            status.put("redis", "disconnected: " + e.getMessage());
        }
        
        status.put("service", "running");
        
        BaseResponse<Map<String, String>> response = BaseResponse.<Map<String, String>>builder()
                .msg("服务运行正常")
                .code("success")
                .data(status)
                .build();
        
        return ResponseEntity.ok(response);
    }
} 