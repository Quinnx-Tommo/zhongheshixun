package com.training.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.dto.RoleForm;
import com.training.common.dto.RolePermissionDTO;
import com.training.common.dto.RoleQuery;
import com.training.common.entity.SysRole;
import com.training.common.result.Result;
import com.training.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 角色管理控制器（后台管理端）
 * <p>RBAC 4 表中的 sys_role 维护入口，支持角色 CRUD + 权限分配。</p>
 * <p>权限码复用 user:read / user:write（角色管理属于用户管理延伸），
 * 兜底由 SecurityConfig 中 /admin/** hasRole('ADMIN') 控制。</p>
 */
@Slf4j
@RestController
@RequestMapping("/admin/role")
public class RoleController {

    @Resource
    private SysRoleService roleService;

    /**
     * 分页列表（支持 roleCode 模糊 + status 筛选）
     */
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/page")
    public Result<IPage<SysRole>> page(RoleQuery query) {
        return Result.success(roleService.page(query));
    }

    /**
     * 角色详情
     */
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/{id}")
    public Result<SysRole> detail(@PathVariable Long id) {
        SysRole role = roleService.getRoleDetail(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }
        return Result.success(role);
    }

    /**
     * 查询所有启用角色（下拉框用）
     */
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.listAllEnabled());
    }

    /**
     * 创建角色
     */
    @PreAuthorize("hasAuthority('user:write')")
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid RoleForm form) {
        boolean ok = roleService.createRole(form);
        return Result.success(ok);
    }

    /**
     * 编辑角色
     */
    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping
    public Result<Boolean> update(@RequestBody @Valid RoleForm form) {
        boolean ok = roleService.updateRole(form);
        return Result.success(ok);
    }

    /**
     * 删除角色（逻辑删除；内置角色 ADMIN/TEACHER/STUDENT 禁止删除）
     */
    @PreAuthorize("hasAuthority('user:write')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = roleService.deleteRole(id);
        return Result.success(ok);
    }

    /**
     * 查询角色当前已分配的权限信息
     */
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/{id}/permissions")
    public Result<RolePermissionDTO> getPermissions(@PathVariable Long id) {
        return Result.success(roleService.getRolePermissions(id));
    }

    /**
     * 为角色重新分配权限（全量覆盖）
     * <p>请求体为权限ID列表，如 [1,2,3,14,15]；空列表表示移除所有权限。</p>
     */
    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/permissions")
    public Result<Boolean> assignPermissions(@PathVariable Long id,
                                             @RequestBody List<Long> permissionIds) {
        boolean ok = roleService.assignPermissions(id, permissionIds);
        return Result.success(ok);
    }
}
