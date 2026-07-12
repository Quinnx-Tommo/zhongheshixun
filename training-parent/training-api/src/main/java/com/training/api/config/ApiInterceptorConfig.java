package com.training.api.config;

import com.training.api.interceptor.ApiJwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * API 拦截器配置
 *
 * 规则：
 * - 放行公开接口：/api/wx/login、/api/course/**
 * - 拦截需要登录：/api/user/**、/api/study/**、/api/exam/**、/api/**
 */
@Configuration
public class ApiInterceptorConfig implements WebMvcConfigurer {

    @Resource
    private ApiJwtInterceptor apiJwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiJwtInterceptor)
                .addPathPatterns(
                        "/api/user/**",
                        "/api/study/**",
                        "/api/exam/**",
                        "/api/consult/**",
                        "/api/stats/**"
                )
                .excludePathPatterns(
                        "/api/wx/login",
                        "/api/course/**",
                        "/error"
                );
    }
}
