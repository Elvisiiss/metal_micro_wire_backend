package com.mmw.metal_micro_wire_backend.config;

import com.mmw.metal_micro_wire_backend.interceptor.AuthInterceptor;
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
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 需要认证的路径
                .addPathPatterns("/api/**")
                // 排除不需要认证的路径
                .excludePathPatterns(
                        // 认证相关接口不需要token验证（但排除需要认证的特定接口）
                        "/api/auth/register/**",
                        "/api/auth/login/**", 
                        "/api/auth/reset-password/**",
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
                );
    }
}
 