package com.training.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.training.common.dto.RoleForm;
import com.training.common.dto.RolePermissionDTO;
import com.training.common.dto.RoleQuery;
import com.training.common.entity.SysPermission;
import com.training.common.entity.SysRole;
import com.training.mapper.SysPermissionMapper;
import com.training.mapper.SysRoleMapper;
import com.training.mapper.SysRolePermissionMapper;
import com.training.service.SysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysPermissionMapper sysPermissionMapper;

    @Resource
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public IPage<SysRole> page(RoleQuery query) {
        Page<SysRole> page = new Page<>(query.getPageNum(), query.getPageSize());
        return sysRoleMapper.selectRolePage(page, query.getRoleCode(), query.getStatus());
    }

    @Override
    public SysRole getRoleDetail(Long id) {
        return sysRoleMapper.selectById(id);
    }

    @Override
    public List<SysRole> listAllEnabled() {
        return sysRoleMapper.selectAllEnabled();
    }

    @Override
    public boolean createRole(RoleForm form) {
        // 校验角色编码唯一
        SysRole exist = sysRoleMapper.selectByCode(form.getRoleCode());
        if (exist != null) {
            throw new IllegalArgumentException("角色编码已存在：" + form.getRoleCode());
        }
        SysRole role = new SysRole();
        role.setRoleCode(form.getRoleCode());
        role.setRoleName(form.getRoleName());
        role.setDescription(form.getDescription());
        role.setStatus(form.getStatus() == null ? 1 : form.getStatus());
        boolean ok = save(role);
        // 创建时若提供 permissionIds，则同步分配权限
        if (ok && form.getPermissionIds() != null && !form.getPermissionIds().isEmpty()) {
            assignPermissions(role.getId(), form.getPermissionIds());
        }
        return ok;
    }

    @Override
    public boolean updateRole(RoleForm form) {
        if (form.getId() == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        SysRole exist = getById(form.getId());
        if (exist == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        // 角色编码唯一校验（排除自己）
        if (StringUtils.hasText(form.getRoleCode()) && !form.getRoleCode().equals(exist.getRoleCode())) {
            SysRole sameCode = sysRoleMapper.selectByCode(form.getRoleCode());
            if (sameCode != null && !sameCode.getId().equals(form.getId())) {
                throw new IllegalArgumentException("角色编码已存在：" + form.getRoleCode());
            }
            exist.setRoleCode(form.getRoleCode());
        }
        if (form.getRoleName() != null) {
            exist.setRoleName(form.getRoleName());
        }
        if (form.getDescription() != null) {
            exist.setDescription(form.getDescription());
        }
        if (form.getStatus() != null) {
            exist.setStatus(form.getStatus());
        }
        boolean ok = updateById(exist);
        // 编辑时若提供 permissionIds，则同步覆盖权限
        if (ok && form.getPermissionIds() != null) {
            assignPermissions(form.getId(), form.getPermissionIds());
        }
        return ok;
    }

    @Override
    public boolean deleteRole(Long id) {
        // 内置角色（ADMIN/TEACHER/STUDENT）禁止删除
        SysRole role = getById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        String code = role.getRoleCode();
        if ("ADMIN".equals(code) || "TEACHER".equals(code) || "STUDENT".equals(code)) {
            throw new IllegalArgumentException("内置角色禁止删除：" + code);
        }
        // 逻辑删除角色
        boolean ok = removeById(id);
        // 同步清理角色-权限关联（物理删除，避免脏数据）
        if (ok) {
            sysRolePermissionMapper.deleteByRoleId(id);
        }
        return ok;
    }

    @Override
    public RolePermissionDTO getRolePermissions(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        RolePermissionDTO dto = new RolePermissionDTO();
        dto.setRoleId(role.getId());
        dto.setRoleCode(role.getRoleCode());
        dto.setRoleName(role.getRoleName());

        List<SysPermission> perms = sysPermissionMapper.selectByRoleId(roleId);
        dto.setPermCodes(perms.stream().map(SysPermission::getPermCode).collect(Collectors.toList()));
        dto.setPermIds(perms.stream().map(SysPermission::getId).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        // 1. 先删除旧关联
        sysRolePermissionMapper.deleteByRoleId(roleId);
        // 2. 再批量插入新关联（permissionIds 为空时仅清空，相当于移除所有权限）
        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }
        // 去重
        List<Long> distinctIds = permissionIds.stream().distinct().collect(Collectors.toList());
        return sysRolePermissionMapper.insertBatch(roleId, distinctIds) > 0;
    }
}
