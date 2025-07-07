package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.user.AvatarUploadResponse;
import com.mmw.metal_micro_wire_backend.dto.user.UpdateUsernameRequest;
import com.mmw.metal_micro_wire_backend.dto.user.UserProfileResponse;
import com.mmw.metal_micro_wire_backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



/**
 * 用户控制器
 * 需要认证的接口示例
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    
    /**
     * 获取当前用户详细信息
     * 这个接口需要token认证
     */
    @GetMapping("/profile")
    public ResponseEntity<BaseResponse<UserProfileResponse>> getUserProfile(HttpServletRequest request) {
        // 从拦截器设置的属性中获取用户信息
        Long userId = (Long) request.getAttribute("userId");

        BaseResponse<UserProfileResponse> response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 上传用户头像
     * 这个接口需要token认证
     */
    @PostMapping("/avatar/upload")
    public ResponseEntity<BaseResponse<AvatarUploadResponse>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        BaseResponse<AvatarUploadResponse> response = userService.uploadAvatar(userId, file);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除用户头像
     * 这个接口需要token认证
     */
    @DeleteMapping("/avatar")
    public ResponseEntity<BaseResponse<Void>> deleteAvatar(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BaseResponse<Void> response = userService.deleteAvatar(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 修改用户名
     * 这个接口需要token认证，用户只能修改自己的用户名
     */
    @PutMapping("/username")
    public ResponseEntity<BaseResponse<Void>> updateUsername(
            @Valid @RequestBody UpdateUsernameRequest request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        BaseResponse<Void> response = userService.updateUsername(userId, request);
        return ResponseEntity.ok(response);
    }
} 