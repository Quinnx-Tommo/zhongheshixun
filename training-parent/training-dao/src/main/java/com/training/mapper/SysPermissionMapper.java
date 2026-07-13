package com.training.mapper;

import com.training.common.entity.SysPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /**
     * 按权限编码查询权限
     *
     * @param permCode 权限编码，如 user:read
     * @return 权限实体，未找到返回 null
     */
    SysPermission selectByCode(@Param("permCode") String permCode);

    /**
     * 分页查询权限列表（支持 module 精确 + permCode 模糊筛选）
     *
     * @param page     分页对象
     * @param module   模块名（可空）
     * @param permCode 权限编码模糊关键字（可空）
     * @return 分页结果
     */
    IPage<SysPermission> selectPermissionPage(IPage<SysPermission> page,
                                              @Param("module") String module,
                                              @Param("permCode") String permCode);

    /**
     * 查询所有未删除的权限（按 module 分组展示用）
     *
     * @return 权限列表
     */
    List<SysPermission> selectAll();
}
