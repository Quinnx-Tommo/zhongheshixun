package com.training.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.dto.PermissionForm;
import com.training.common.dto.PermissionQuery;
import com.training.common.entity.SysPermission;
import com.training.common.result.Result;
import com.training.service.SysPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 权限管理控制器（后台管理端）
 * <p>RBAC 4 表中的 sys_permission 维护入口，支持权限 CRUD。</p>
 * <p>权限码复用 user:read / user:write（权限管理属于用户管理延伸），
 * 兜底由 SecurityConfig 中 /admin/** hasRole('ADMIN') 控制。</p>
 */
@Slf4j
@RestController
@RequestMapping("/admin/permission")
public class PermissionController {

    @Resource
    private SysPermissionService permissionService;

    /**
     * 分页列表（支持 module 精确 + permCode 模糊筛选）
     */
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/page")
    public Result<IPage<SysPermission>> page(PermissionQuery query) {
        return Result.success(permissionService.page(query));
    }

    /**
     * 权限详情
     */
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/{id}")
    public Result<SysPermission> detail(@PathVariable Long id) {
        SysPermission perm = permissionService.getPermissionDetail(id);
        if (perm == null) {
            return Result.error(404, "权限不存在");
        }
        return Result.success(perm);
    }

    /**
     * 查询所有权限（按 module 分组展示用）
     */
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/list")
    public Result<List<SysPermission>> list() {
        return Result.success(permissionService.listAll());
    }

    /**
     * 创建权限
     */
    @PreAuthorize("hasAuthority('user:write')")
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid PermissionForm form) {
        boolean ok = permissionService.createPermission(form);
        return Result.success(ok);
    }

    /**
     * 编辑权限
     */
    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping
    public Result<Boolean> update(@RequestBody @Valid PermissionForm form) {
        boolean ok = permissionService.updatePermission(form);
        return Result.success(ok);
    }

    /**
     * 删除权限（逻辑删除）
     */
    @PreAuthorize("hasAuthority('user:write')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = permissionService.deletePermission(id);
        return Result.success(ok);
    }
}
