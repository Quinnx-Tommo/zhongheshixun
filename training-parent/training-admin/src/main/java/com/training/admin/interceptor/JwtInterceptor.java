package com.training.admin.interceptor;

import com.training.common.constants.CommonConstants;
import com.training.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 拦截器（已废弃，由 JwtAuthenticationFilter 替代）
 * <p>保留文件作备查，未注册为 Bean。</p>
 */
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行登录等白名单接口
        String uri = request.getRequestURI();
        if (uri.contains("/login") || uri.contains("/wx/login") || uri.equals("/error")) {
            return true;
        }

        // 2. 解析 Authorization Header
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith(CommonConstants.BEARER_PREFIX)) {
            token = token.substring(7);
        }

        // 3. 校验 Token 有效性
        if (token == null || token.isEmpty() || !jwtUtils.validate(token)) {
            writeError(response, 401, "未登录或登录已过期");
            return false;
        }

        // 4. 将用户信息注入 request attribute
        Claims claims = jwtUtils.parse(token);
        request.setAttribute("userId", claims.get("userId", Long.class));
        request.setAttribute("role", claims.get("role", String.class));
        request.setAttribute("username", claims.get("username", String.class));
        return true;
    }

    private void writeError(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + msg + "\"}");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理 ThreadLocal（如有）
    }
}
