package com.mmw.metal_micro_wire_backend.interceptor;

import com.mmw.metal_micro_wire_backend.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * 用于验证需要登录的接口的token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    
    private final TokenService tokenService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跨域预检请求直接放行（如果前端未使用代理）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        // 从请求头中获取token
        String token = getTokenFromRequest(request);
        
        if (token == null || token.trim().isEmpty()) {
            log.warn("请求缺少token，URI：{}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"Error\",\"msg\":\"未提供认证token\"}");
            return false;
        }
        
        // 验证token
        if (!tokenService.validateToken(token)) {
            log.warn("Token验证失败，URI：{}，Token：{}", request.getRequestURI(), token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"Error\",\"msg\":\"token无效或已过期\"}");
            return false;
        }
        
        // 将用户信息设置到请求属性中，供后续处理使用
        Long userId = tokenService.getUserIdFromToken(token);
        String email = tokenService.getEmailFromToken(token);
        String userName = tokenService.getUserNameFromToken(token);
        Integer roleId = tokenService.getRoleIdFromToken(token);
        
        request.setAttribute("userId", userId);
        request.setAttribute("email", email);
        request.setAttribute("userName", userName);
        request.setAttribute("roleId", roleId);
        request.setAttribute("token", token);
        
        log.debug("Token验证成功，用户ID：{}，邮箱：{}，URI：{}", userId, email, request.getRequestURI());
        return true;
    }
    
    /**
     * 从请求中获取token
     * 优先从Authorization头获取，格式：Bearer token
     * 如果没有，则从参数token中获取
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 从Authorization头获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 从参数获取
        String token = request.getParameter("token");
        if (token != null && !token.trim().isEmpty()) {
            return token;
        }
        
        // 从header直接获取token
        return request.getHeader("token");
    }
} 