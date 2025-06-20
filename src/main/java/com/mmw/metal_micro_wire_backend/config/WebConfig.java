package com.mmw.metal_micro_wire_backend.config;

import com.mmw.metal_micro_wire_backend.interceptor.AuthInterceptor;
import com.mmw.metal_micro_wire_backend.interceptor.RootAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 用于配置拦截器和其他Web相关配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final AuthInterceptor authInterceptor;
    private final RootAuthInterceptor rootAuthInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 基础认证拦截器 - 验证Token有效性
        registry.addInterceptor(authInterceptor)
                // 需要认证的路径
                .addPathPatterns("/api/**")
                // 排除不需要认证的路径
                .excludePathPatterns(
                        // 认证相关接口不需要token验证（但排除需要认证的特定接口）
                        "/api/auth/register/**",
                        "/api/auth/login/**", 
                        "/api/auth/reset-password/**",
                        "/api/auth/root/**",
                        "/api/auth/token",
                        // 注意：/api/auth/user/data 和 /api/auth/logout 需要认证，所以不排除
                        
                        // 静态资源
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        // Swagger相关
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        // 健康检查
                        "/api/health",
                        // 错误页面
                        "/error"
                )
                .order(0); // 基础认证拦截器优先级最高
        
        // 2. Root权限拦截器 - 验证Root用户权限（在基础认证之后执行）
        registry.addInterceptor(rootAuthInterceptor)
                .addPathPatterns("/api/root/**")
                .order(1); // 确保在AuthInterceptor之后执行
    }
}
 