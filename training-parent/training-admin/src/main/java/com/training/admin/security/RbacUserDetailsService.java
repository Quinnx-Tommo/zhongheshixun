package com.training.admin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.training.common.entity.SysPermission;
import com.training.common.entity.SysRole;
import com.training.common.entity.SysUser;
import com.training.mapper.SysPermissionMapper;
import com.training.mapper.SysRoleMapper;
import com.training.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RBAC 用户详情服务
 * <p>按用户名加载用户 + 角色 + 权限，组装 UserDetails。</p>
 */
@Service
public class RbacUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(RbacUserDetailsService.class);

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysPermissionMapper sysPermissionMapper;

    /**
     * 按用户名加载用户详情
     *
     * @param username 用户名
     * @return UserDetails
     * @throws UsernameNotFoundException 用户不存在或已删除/禁用
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查用户（未删除）
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            log.warn("[RBAC] 用户不存在或已删除：{}", username);
            throw new UsernameNotFoundException("用户不存在");
        }

        // 2. 校验状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            log.warn("[RBAC] 用户已禁用：{}", username);
            throw new UsernameNotFoundException("账号已被禁用");
        }

        // 3. 优先按 roleId 查角色，回退到 role 字符串（向前兼容）
        String roleCode = null;
        Long roleId = user.getRoleId();
        if (roleId != null) {
            SysRole role = sysRoleMapper.selectById(roleId);
            if (role != null) {
                roleCode = role.getRoleCode();
            }
        } else if (user.getRole() != null && !user.getRole().isEmpty()) {
            // 兼容旧数据：按 role 字符串直接当 roleCode
            roleCode = user.getRole().toUpperCase();
        }

        if (roleCode == null) {
            log.warn("[RBAC] 用户 {} 未分配角色", username);
            throw new UsernameNotFoundException("用户未分配角色");
        }

        // 4. 查权限列表
        List<String> permissionCodes = null;
        if (roleId != null) {
            List<SysPermission> perms = sysPermissionMapper.selectByRoleId(roleId);
            permissionCodes = perms.stream()
                    .map(SysPermission::getPermCode)
                    .collect(Collectors.toList());
        }
        // roleId 为空时（旧数据），无细粒度权限，仅靠 URL 粗粒度 ROLE_ADMIN 控制

        // 5. 组装 LoginUser
        LoginUser loginUser = new LoginUser(user, roleCode, permissionCodes);
        log.debug("[RBAC] 加载用户 {} 完成，角色 {}，权限 {} 条",
                username, roleCode, permissionCodes == null ? 0 : permissionCodes.size());
        return loginUser;
    }
}
