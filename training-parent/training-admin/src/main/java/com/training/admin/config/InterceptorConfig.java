package com.training.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置（RBAC Phase 2 后清空，鉴权由 Spring Security 过滤器链负责）
 * <p>避免 JwtInterceptor 与 JwtAuthenticationFilter 双重鉴权。</p>
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    /**
     * 不再注册旧拦截器，鉴权统一走 SecurityFilterChain
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 清空：鉴权已迁移至 Spring Security
    }
}
