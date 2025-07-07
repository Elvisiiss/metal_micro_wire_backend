package com.mmw.metal_micro_wire_backend.controller;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.chat.*;
import com.mmw.metal_micro_wire_backend.service.ChatService;
import com.mmw.metal_micro_wire_backend.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    
    /**
     * 发送聊天消息
     */
    @PostMapping("/message")
    public ResponseEntity<BaseResponse<ChatMessageResponse>> sendMessage(
            @Valid @RequestBody ChatMessageRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        TokenService.UserType userType = (TokenService.UserType) httpRequest.getAttribute("userType");
        String userName = (String) httpRequest.getAttribute("userName");
        
        log.info("用户发送聊天消息，用户：{}(ID:{})，用户类型：{}，会话ID：{}", 
                userName, userId, userType, request.getSessionId());
        
        try {
            ChatMessageResponse response = chatService.sendMessage(userId, userType, request);
            return ResponseEntity.ok(BaseResponse.success("发送成功", response));
        } catch (Exception e) {
            log.error("发送聊天消息失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.error("发送失败：" + e.getMessage()));
        }
    }
    
    /**
     * 创建新的聊天会话
     */
    @PostMapping("/session")
    public ResponseEntity<BaseResponse<ChatSessionResponse>> createSession(
            @Valid @RequestBody CreateChatSessionRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        TokenService.UserType userType = (TokenService.UserType) httpRequest.getAttribute("userType");
        String userName = (String) httpRequest.getAttribute("userName");
        
        log.info("用户创建聊天会话，用户：{}(ID:{})，用户类型：{}，标题：{}", 
                userName, userId, userType, request.getTitle());
        
        try {
            ChatSessionResponse response = chatService.createSession(userId, userType, request);
            return ResponseEntity.ok(BaseResponse.success("创建成功", response));
        } catch (Exception e) {
            log.error("创建聊天会话失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.error("创建失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取用户的聊天会话列表
     */
    @GetMapping("/sessions")
    public ResponseEntity<BaseResponse<ChatSessionListResponse>> getUserSessions(
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        TokenService.UserType userType = (TokenService.UserType) httpRequest.getAttribute("userType");
        String userName = (String) httpRequest.getAttribute("userName");
        
        log.info("用户获取聊天会话列表，用户：{}(ID:{})，用户类型：{}", userName, userId, userType);
        
        try {
            ChatSessionListResponse response = chatService.getUserSessions(userId, userType);
            return ResponseEntity.ok(BaseResponse.success("获取成功", response));
        } catch (Exception e) {
            log.error("获取聊天会话列表失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.error("获取失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取聊天历史记录
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<BaseResponse<ChatHistoryResponse>> getChatHistory(
            @PathVariable String sessionId,
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        TokenService.UserType userType = (TokenService.UserType) httpRequest.getAttribute("userType");
        String userName = (String) httpRequest.getAttribute("userName");
        
        log.info("用户获取聊天历史，用户：{}(ID:{})，用户类型：{}，会话ID：{}", 
                userName, userId, userType, sessionId);
        
        try {
            ChatHistoryResponse response = chatService.getChatHistory(userId, userType, sessionId);
            return ResponseEntity.ok(BaseResponse.success("获取成功", response));
        } catch (Exception e) {
            log.error("获取聊天历史失败，用户ID：{}，会话ID：{}，错误：{}", userId, sessionId, e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.error("获取失败：" + e.getMessage()));
        }
    }
    
    /**
     * 删除聊天会话
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<BaseResponse<Void>> deleteSession(
            @PathVariable String sessionId,
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        TokenService.UserType userType = (TokenService.UserType) httpRequest.getAttribute("userType");
        String userName = (String) httpRequest.getAttribute("userName");
        
        log.info("用户删除聊天会话，用户：{}(ID:{})，用户类型：{}，会话ID：{}", 
                userName, userId, userType, sessionId);
        
        try {
            chatService.deleteSession(userId, userType, sessionId);
            return ResponseEntity.ok(BaseResponse.success("删除成功"));
        } catch (Exception e) {
            log.error("删除聊天会话失败，用户ID：{}，会话ID：{}，错误：{}", userId, sessionId, e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.error("删除失败：" + e.getMessage()));
        }
    }
    
    /**
     * 更新会话标题
     */
    @PutMapping("/session/{sessionId}/title")
    public ResponseEntity<BaseResponse<Void>> updateSessionTitle(
            @PathVariable String sessionId,
            @RequestParam String title,
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        TokenService.UserType userType = (TokenService.UserType) httpRequest.getAttribute("userType");
        String userName = (String) httpRequest.getAttribute("userName");
        
        log.info("用户更新会话标题，用户：{}(ID:{})，用户类型：{}，会话ID：{}，新标题：{}", 
                userName, userId, userType, sessionId, title);
        
        try {
            chatService.updateSessionTitle(userId, userType, sessionId, title);
            return ResponseEntity.ok(BaseResponse.success("更新成功"));
        } catch (Exception e) {
            log.error("更新会话标题失败，用户ID：{}，会话ID：{}，错误：{}", userId, sessionId, e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.error("更新失败：" + e.getMessage()));
        }
    }
    
    /**
     * 清理过期会话
     */
    @DeleteMapping("/sessions/cleanup")
    public ResponseEntity<BaseResponse<Void>> cleanupExpiredSessions(
            HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        TokenService.UserType userType = (TokenService.UserType) httpRequest.getAttribute("userType");
        String userName = (String) httpRequest.getAttribute("userName");
        
        log.info("用户清理过期会话，用户：{}(ID:{})，用户类型：{}", userName, userId, userType);
        
        try {
            chatService.cleanupExpiredSessions(userId, userType);
            return ResponseEntity.ok(BaseResponse.success("清理成功"));
        } catch (Exception e) {
            log.error("清理过期会话失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            return ResponseEntity.ok(BaseResponse.error("清理失败：" + e.getMessage()));
        }
    }
} 