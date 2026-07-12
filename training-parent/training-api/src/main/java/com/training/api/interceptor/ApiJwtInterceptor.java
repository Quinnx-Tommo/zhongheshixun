package com.training.api.interceptor;

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
 * 小程序 API JWT 拦截器
 */
@Component
public class ApiJwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        // 放行登录接口
        if (uri.contains("/wx/login") || uri.equals("/error")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith(CommonConstants.BEARER_PREFIX)) {
            token = token.substring(7);
        }

        if (token == null || token.isEmpty() || !jwtUtils.validate(token)) {
            writeError(response, 401, "未登录或登录已过期");
            return false;
        }

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
}
