package com.training.admin.config;

import com.training.admin.security.CustomAccessDeniedHandler;
import com.training.admin.security.CustomAuthenticationEntryPoint;
import com.training.admin.security.JwtAuthenticationFilter;
import com.training.admin.security.RbacUserDetailsService;
import com.training.common.utils.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;

/**
 * Spring Security 核心配置（RBAC Phase 2）
 * <p>启用方法级鉴权 + URL 粗粒度规则 + JWT 过滤器链。</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RbacUserDetailsService userDetailsService;

    /**
     * 安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 401 / 403 处理器
        CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint();
        CustomAccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler();

        // JWT 过滤器
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtils, userDetailsService);

        http
                // 禁用 CSRF（JWT 天然防 CSRF）
                .csrf(csrf -> csrf.disable())
                // 无状态 session
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URL 鉴权规则（Spring Security 5.7 用 antMatchers）
                .authorizeHttpRequests(auth -> auth
                        // 白名单放行
                        .antMatchers(
                                "/admin/login",
                                "/api/wx/login",
                                "/error",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // M12 修复：讲师 P2/P3 跳 5176 后可以查看教学相关路径（只读 + 自身范围）
                        // 允许 TEACHER 角色访问教学模块
                        .antMatchers("/admin/course", "/admin/course/page", "/admin/course/{id}").hasAnyRole("ADMIN", "TEACHER")
                        .antMatchers("/admin/question", "/admin/question/page", "/admin/question/{id}").hasAnyRole("ADMIN", "TEACHER")
                        .antMatchers("/admin/consult", "/admin/consult/page", "/admin/consult/sla-alert").hasAnyRole("ADMIN", "TEACHER")
                        // 其他后台路径要求 ADMIN 角色
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        // 其他路径需认证
                        .anyRequest().authenticated()
                )
                // 异常处理
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                // JWT 过滤器加在 UsernamePasswordAuthenticationFilter 之前
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
