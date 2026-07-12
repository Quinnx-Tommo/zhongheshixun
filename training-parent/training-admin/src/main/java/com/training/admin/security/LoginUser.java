package com.training.admin.security;

import com.training.common.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetails 实现
 * <p>封装登录用户身份 + 权限集合，供 SecurityContext 使用。</p>
 */
public class LoginUser implements UserDetails {

    /** 用户主键 */
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（BCrypt 密文） */
    private String password;

    /** 角色编码（ADMIN/TEACHER/STUDENT） */
    private String roleCode;

    /** 权限集合（含 ROLE_xxx 前缀角色 + 业务权限码） */
    private List<SimpleGrantedAuthority> authorities;

    /** 是否启用 */
    private boolean enabled;

    public LoginUser() {
    }

    public LoginUser(SysUser user, String roleCode, List<String> permissionCodes) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.roleCode = roleCode;
        this.enabled = user.getStatus() != null && user.getStatus() == 1;

        // 合并角色（加 ROLE_ 前缀）+ 业务权限码
        this.authorities = new java.util.ArrayList<>();
        if (roleCode != null && !roleCode.isEmpty()) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode));
        }
        if (permissionCodes != null) {
            for (String code : permissionCodes) {
                if (code != null && !code.isEmpty()) {
                    this.authorities.add(new SimpleGrantedAuthority(code));
                }
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<SimpleGrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        // 本期不做账号过期
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 本期不做账号锁定
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 本期不做凭证过期
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
