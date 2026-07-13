package com.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.training.common.dto.PermissionForm;
import com.training.common.dto.PermissionQuery;
import com.training.common.entity.SysPermission;

import java.util.List;

/**
 * 权限管理服务接口
 */
public interface SysPermissionService extends IService<SysPermission> {

    /**
     * 分页查询权限列表
     */
    IPage<SysPermission> page(PermissionQuery query);

    /**
     * 查询权限详情
     */
    SysPermission getPermissionDetail(Long id);

    /**
     * 查询所有未删除权限（按 module 分组展示用）
     */
    List<SysPermission> listAll();

    /**
     * 创建权限
     */
    boolean createPermission(PermissionForm form);

    /**
     * 编辑权限
     */
    boolean updatePermission(PermissionForm form);

    /**
     * 删除权限（逻辑删除）
     */
    boolean deletePermission(Long id);
}
