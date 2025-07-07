package com.mmw.metal_micro_wire_backend.service;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.user.AvatarUploadResponse;
import com.mmw.metal_micro_wire_backend.dto.user.UpdateUsernameRequest;
import com.mmw.metal_micro_wire_backend.dto.user.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 获取用户详细资料
     * 
     * @param userId 用户ID
     * @return 用户资料信息
     */
    BaseResponse<UserProfileResponse> getUserProfile(Long userId);
    
    /**
     * 上传用户头像
     * 
     * @param userId 用户ID
     * @param file 头像文件
     * @return 上传结果
     */
    BaseResponse<AvatarUploadResponse> uploadAvatar(Long userId, MultipartFile file);
    
    /**
     * 删除用户头像
     * 
     * @param userId 用户ID
     * @return 删除结果
     */
    BaseResponse<Void> deleteAvatar(Long userId);
    
    /**
     * 修改用户名
     * 
     * @param userId 用户ID
     * @param request 修改请求
     * @return 修改结果
     */
    BaseResponse<Void> updateUsername(Long userId, UpdateUsernameRequest request);
}
