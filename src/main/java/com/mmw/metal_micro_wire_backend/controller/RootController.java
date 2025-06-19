package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.auth.UserInfoResponse;
import com.mmw.metal_micro_wire_backend.dto.auth.UserManageRequest;
import com.mmw.metal_micro_wire_backend.entity.User;
import com.mmw.metal_micro_wire_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Root用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/root")
@RequiredArgsConstructor
@Validated
public class RootController {
    
    private final UserRepository userRepository;
    
    /**
     * 获取所有用户列表（分页）
     */
    @GetMapping("/users")
    public ResponseEntity<BaseResponse<Page<UserInfoResponse>>> getUserList(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        
        String rootUserName = (String) request.getAttribute("userName");
        Long rootUserId = (Long) request.getAttribute("userId");
        log.info("Root用户获取用户列表，Root用户：{}(ID:{}), 页码：{}，大小：{}，关键词：{}", 
                rootUserName, rootUserId, page, size, keyword);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
            Page<User> userPage;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                userPage = userRepository.findByUserNameContainingOrEmailContaining(keyword.trim(), pageable);
            } else {
                userPage = userRepository.findAll(pageable);
            }
            
            Page<UserInfoResponse> responsePage = userPage.map(this::convertToUserInfoResponse);
            
            return ResponseEntity.ok(BaseResponse.success("获取成功", responsePage));
            
        } catch (Exception e) {
            log.error("获取用户列表失败，错误：{}", e.getMessage());
            return ResponseEntity.ok(BaseResponse.error("获取用户列表失败"));
        }
    }
    
    /**
     * 用户提权（设置角色）
     */
    @PutMapping("/users/role")
    public ResponseEntity<BaseResponse<Void>> updateUserRole(@Valid @RequestBody UserManageRequest request, 
                                                             HttpServletRequest httpRequest) {
        String rootUserName = (String) httpRequest.getAttribute("userName");
        Long rootUserId = (Long) httpRequest.getAttribute("userId");
        log.info("Root用户提权操作，Root用户：{}(ID:{}), 目标用户ID：{}，角色ID：{}", 
                rootUserName, rootUserId, request.getUserId(), request.getRoleId());
        
        try {
            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok(BaseResponse.error("用户不存在"));
            }
            
            User user = userOpt.get();
            user.setRoleId(request.getRoleId());
            userRepository.save(user);
            
            String roleName = request.getRoleId() == 0 ? "普通用户" : "管理员";
            log.info("用户提权成功，用户：{}，角色：{}", user.getUserName(), roleName);
            
            return ResponseEntity.ok(BaseResponse.success("用户角色更新成功，当前角色：" + roleName));
            
        } catch (Exception e) {
            log.error("用户提权失败，用户ID：{}，错误：{}", request.getUserId(), e.getMessage());
            return ResponseEntity.ok(BaseResponse.error("用户角色更新失败"));
        }
    }
    
    /**
     * 禁用/启用用户
     */
    @PutMapping("/users/status")
    public ResponseEntity<BaseResponse<Void>> updateUserStatus(@Valid @RequestBody UserManageRequest request,
                                                               HttpServletRequest httpRequest) {
        String rootUserName = (String) httpRequest.getAttribute("userName");
        Long rootUserId = (Long) httpRequest.getAttribute("userId");
        log.info("Root用户状态操作，Root用户：{}(ID:{}), 目标用户ID：{}，状态：{}", 
                rootUserName, rootUserId, request.getUserId(), request.getStatus());
        
        try {
            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok(BaseResponse.error("用户不存在"));
            }
            
            User user = userOpt.get();
            user.setStatus(request.getStatus());
            userRepository.save(user);
            
            String statusName = request.getStatus() == 0 ? "启用" : "禁用";
            log.info("用户状态更新成功，用户：{}，状态：{}", user.getUserName(), statusName);
            
            return ResponseEntity.ok(BaseResponse.success("用户已" + statusName));
            
        } catch (Exception e) {
            log.error("用户状态更新失败，用户ID：{}，错误：{}", request.getUserId(), e.getMessage());
            return ResponseEntity.ok(BaseResponse.error("用户状态更新失败"));
        }
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<BaseResponse<UserInfoResponse>> getUserDetail(@PathVariable @Min(1) Long userId) {
        log.info("Root获取用户详情，用户ID：{}", userId);
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok(BaseResponse.error("用户不存在"));
            }
            
            UserInfoResponse userInfo = convertToUserInfoResponse(userOpt.get());
            return ResponseEntity.ok(BaseResponse.success("获取成功", userInfo));
            
        } catch (Exception e) {
            log.error("获取用户详情失败，用户ID：{}，错误：{}", userId, e.getMessage());
            return ResponseEntity.ok(BaseResponse.error("获取用户详情失败"));
        }
    }
    
    /**
     * 批量用户操作
     */
    @PutMapping("/users/batch")
    public ResponseEntity<BaseResponse<Void>> batchUpdateUsers(
            @RequestParam String action,
            @RequestParam String userIds) {
        
        log.info("Root批量用户操作，操作：{}，用户IDs：{}", action, userIds);
        
        try {
            String[] idArray = userIds.split(",");
            int successCount = 0;
            
            for (String idStr : idArray) {
                try {
                    Long userId = Long.parseLong(idStr.trim());
                    Optional<User> userOpt = userRepository.findById(userId);
                    
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        
                        switch (action) {
                            case "disable" -> user.setStatus(1);
                            case "enable" -> user.setStatus(0);
                            case "promote" -> user.setRoleId(1);
                            case "demote" -> user.setRoleId(0);
                            default -> {
                                continue;
                            }
                        }
                        
                        userRepository.save(user);
                        successCount++;
                    }
                } catch (NumberFormatException e) {
                    log.warn("无效的用户ID：{}", idStr);
                }
            }
            
            return ResponseEntity.ok(BaseResponse.success("批量操作完成，成功处理 " + successCount + " 个用户"));
            
        } catch (Exception e) {
            log.error("批量用户操作失败，错误：{}", e.getMessage());
            return ResponseEntity.ok(BaseResponse.error("批量操作失败"));
        }
    }
    
    /**
     * 转换User实体为UserInfoResponse
     */
    private UserInfoResponse convertToUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .roleId(user.getRoleId())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
} 