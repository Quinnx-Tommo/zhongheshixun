package com.training.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类（HMAC-SHA256）
 */
@Component
public class JwtUtils {

    @Value("${training.jwt.secret}")
    private String secret;

    @Value("${training.jwt.expire}")
    private long expire;

    /**
     * 由 secret 字符串生成 HMAC-SHA256 密钥
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token（兼容旧接口，roleId 为 null 时不写入）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色 admin/teacher/student
     */
    public String generate(Long userId, String username, String role) {
        return generate(userId, username, role, null);
    }

    /**
     * 生成 Token（含 roleId）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色 admin/teacher/student（旧字段，向前兼容）
     * @param roleId   角色ID（Phase 1 迁移后使用）
     */
    public String generate(Long userId, String username, String role, Long roleId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        if (roleId != null) {
            claims.put("roleId", roleId);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 获取用户ID
     */
    public Long getUserId(String token) {
        return parse(token).get("userId", Long.class);
    }

    /**
     * 从 Token 获取角色
     */
    public String getRole(String token) {
        return parse(token).get("role", String.class);
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validate(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
