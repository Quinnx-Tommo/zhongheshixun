package com.training.mapper;

import com.training.common.entity.SysPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限 Mapper
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 按角色ID查询拥有的权限列表（JOIN sys_role_permission + sys_permission）
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermission> selectByRoleId(@Param("roleId") Long roleId);
}
