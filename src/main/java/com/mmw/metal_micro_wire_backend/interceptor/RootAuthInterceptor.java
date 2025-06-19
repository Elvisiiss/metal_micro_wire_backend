package com.mmw.metal_micro_wire_backend.interceptor;

import com.mmw.metal_micro_wire_backend.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Root权限拦截器
 * 用于验证访问Root接口的用户必须是Root用户
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RootAuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跨域预检请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        // 从请求属性中获取用户类型（已经在AuthInterceptor中设置）
        TokenService.UserType userType = (TokenService.UserType) request.getAttribute("userType");
        Long userId = (Long) request.getAttribute("userId");
        String userName = (String) request.getAttribute("userName");
        
        // 检查用户类型是否为ROOT
        if (userType != TokenService.UserType.ROOT) {
            log.warn("非Root用户尝试访问Root接口，用户ID：{}，用户名：{}，用户类型：{}，URI：{}", 
                    userId, userName, userType, request.getRequestURI());
            
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"Error\",\"msg\":\"权限不足，仅Root用户可访问此接口\"}");
            return false;
        }
        
        log.debug("Root权限验证通过，用户ID：{}，用户名：{}，URI：{}", userId, userName, request.getRequestURI());
        return true;
    }
} 