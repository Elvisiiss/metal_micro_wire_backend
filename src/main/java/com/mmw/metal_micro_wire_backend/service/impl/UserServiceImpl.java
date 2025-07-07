package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.user.AvatarUploadResponse;
import com.mmw.metal_micro_wire_backend.dto.user.UpdateUsernameRequest;
import com.mmw.metal_micro_wire_backend.dto.user.UserProfileResponse;
import com.mmw.metal_micro_wire_backend.entity.User;
import com.mmw.metal_micro_wire_backend.repository.UserRepository;
import com.mmw.metal_micro_wire_backend.service.UserService;
import com.mmw.metal_micro_wire_backend.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Optional;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final FileUploadUtil fileUploadUtil;
    
    @Override
    public BaseResponse<UserProfileResponse> getUserProfile(Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return BaseResponse.error("用户不存在");
            }
            
            User user = userOpt.get();
            UserProfileResponse response = UserProfileResponse.builder()
                    .userId(user.getId())
                    .userName(user.getUserName())
                    .email(user.getEmail())
                    .roleId(user.getRoleId())
                    .status(user.getStatus())
                    .avatarUrl(user.getAvatarUrl())
                    .createTime(user.getCreateTime())
                    .updateTime(user.getUpdateTime())
                    .build();
            
            log.info("获取用户资料成功，用户ID：{}", userId);
            return BaseResponse.success("获取用户资料成功", response);
            
        } catch (Exception e) {
            log.error("获取用户资料失败，用户ID：{}，错误：{}", userId, e.getMessage());
            return BaseResponse.error("获取用户资料失败");
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<AvatarUploadResponse> uploadAvatar(Long userId, MultipartFile file) {
        try {
            // 检查用户是否存在
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return BaseResponse.error("用户不存在");
            }
            
            User user = userOpt.get();
            
            // 删除旧头像（如果存在）
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty()) {
                fileUploadUtil.deleteAvatar(user.getAvatarUrl());
            }
            
            // 上传新头像
            String avatarUrl = fileUploadUtil.uploadAvatar(file, userId);
            
            // 更新用户头像URL
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
            
            // 构建响应
            AvatarUploadResponse response = AvatarUploadResponse.builder()
                    .avatarUrl(avatarUrl)
                    .fileSize(file.getSize())
                    .originalFilename(file.getOriginalFilename())
                    .uploadTimestamp(Instant.now().toEpochMilli())
                    .build();
            
            log.info("头像上传成功，用户ID：{}，文件大小：{} bytes", userId, file.getSize());
            return BaseResponse.success("头像上传成功", response);
            
        } catch (IllegalArgumentException e) {
            log.warn("头像上传失败，用户ID：{}，错误：{}", userId, e.getMessage());
            return BaseResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("头像上传失败，用户ID：{}，错误：{}", userId, e.getMessage());
            return BaseResponse.error("头像上传失败");
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<Void> deleteAvatar(Long userId) {
        try {
            // 检查用户是否存在
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return BaseResponse.error("用户不存在");
            }
            
            User user = userOpt.get();
            
            // 检查是否有头像
            if (user.getAvatarUrl() == null || user.getAvatarUrl().trim().isEmpty()) {
                return BaseResponse.error("用户暂无头像");
            }
            
            // 删除头像文件
            boolean deleted = fileUploadUtil.deleteAvatar(user.getAvatarUrl());
            
            // 清空数据库中的头像URL
            user.setAvatarUrl(null);
            userRepository.save(user);
            
            if (deleted) {
                log.info("头像删除成功，用户ID：{}", userId);
                return BaseResponse.success("头像删除成功");
            } else {
                log.warn("头像文件删除失败，但数据库记录已清空，用户ID：{}", userId);
                return BaseResponse.success("头像删除成功");
            }
            
        } catch (Exception e) {
            log.error("头像删除失败，用户ID：{}，错误：{}", userId, e.getMessage());
            return BaseResponse.error("头像删除失败");
        }
    }
    
    @Override
    @Transactional
    public BaseResponse<Void> updateUsername(Long userId, UpdateUsernameRequest request) {
        try {
            String newUsername = request.getNewUsername().trim();
            
            // 检查用户名是否包含@符号
            if (newUsername.contains("@")) {
                return BaseResponse.error("用户名不能包含@符号");
            }
            
            // 检查用户是否存在
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return BaseResponse.error("用户不存在");
            }
            
            User user = userOpt.get();
            
            // 检查新用户名是否与当前用户名相同
            if (newUsername.equals(user.getUserName())) {
                return BaseResponse.error("新用户名与当前用户名相同");
            }
            
            // 检查用户名是否已被其他用户使用
            if (userRepository.existsByUserName(newUsername)) {
                return BaseResponse.error("用户名已被使用");
            }
            
            // 更新用户名
            user.setUserName(newUsername);
            userRepository.save(user);
            
            log.info("用户名修改成功，用户ID：{}，新用户名：{}", userId, newUsername);
            return BaseResponse.success("用户名修改成功");
            
        } catch (Exception e) {
            log.error("用户名修改失败，用户ID：{}，错误：{}", userId, e.getMessage());
            return BaseResponse.error("用户名修改失败");
        }
    }
}
