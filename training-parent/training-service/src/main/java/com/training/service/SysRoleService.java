package com.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.training.common.dto.RoleForm;
import com.training.common.dto.RolePermissionDTO;
import com.training.common.dto.RoleQuery;
import com.training.common.entity.SysRole;

import java.util.List;

/**
 * 角色管理服务接口
 * <p>提供角色 CRUD + 角色权限分配查询。</p>
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色列表
     */
    IPage<SysRole> page(RoleQuery query);

    /**
     * 查询角色详情
     */
    SysRole getRoleDetail(Long id);

    /**
     * 查询所有启用角色（下拉框用）
     */
    List<SysRole> listAllEnabled();

    /**
     * 创建角色
     */
    boolean createRole(RoleForm form);

    /**
     * 编辑角色
     */
    boolean updateRole(RoleForm form);

    /**
     * 删除角色（逻辑删除）
     */
    boolean deleteRole(Long id);

    /**
     * 查询角色当前已分配的权限信息
     */
    RolePermissionDTO getRolePermissions(Long roleId);

    /**
     * 为角色重新分配权限（全量覆盖）
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds);
}
