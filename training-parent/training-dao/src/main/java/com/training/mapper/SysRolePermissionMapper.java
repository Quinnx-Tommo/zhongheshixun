package com.training.mapper;

import com.training.common.entity.SysRolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色-权限关联表 Mapper
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    /**
     * 按角色ID删除所有关联（用于重新分配权限）
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 按角色ID查询拥有的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色-权限关联（用于分配权限）
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 影响行数
     */
    int insertBatch(@Param("roleId") Long roleId,
                    @Param("permissionIds") List<Long> permissionIds);
}
