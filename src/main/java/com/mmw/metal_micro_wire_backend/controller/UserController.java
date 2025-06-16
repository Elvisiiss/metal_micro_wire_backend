package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 需要认证的接口示例
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    /**
     * 获取当前用户信息
     * 这个接口需要token认证
     */
    @GetMapping("/profile")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getUserProfile(HttpServletRequest request) {
        // 从拦截器设置的属性中获取用户信息
        Long userId = (Long) request.getAttribute("userId");
        String email = (String) request.getAttribute("email");
        String userName = (String) request.getAttribute("userName");
        Integer roleId = (Integer) request.getAttribute("roleId");
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("email", email);
        userInfo.put("userName", userName);
        userInfo.put("roleId", roleId);
        
        log.info("获取用户信息成功，用户ID：{}，邮箱：{}", userId, email);
        return ResponseEntity.ok(BaseResponse.success("获取用户信息成功", userInfo));
    }
    
    /**
     * 更新用户信息
     * 这个接口需要token认证
     */
    @PostMapping("/update")
    public ResponseEntity<BaseResponse<Void>> updateUser(HttpServletRequest request) {
        // 从拦截器设置的属性中获取用户信息
        Long userId = (Long) request.getAttribute("userId");
        String email = (String) request.getAttribute("email");
        
        // 暂未实现更新逻辑
        log.info("更新用户信息，用户ID：{}，邮箱：{}", userId, email);
        return ResponseEntity.ok(BaseResponse.success("用户信息更新成功"));
    }
} 