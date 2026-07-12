package com.training.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户（关联 sys_role 返回 role/roleName）
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据主键查询用户详情（关联 sys_role 返回 role/roleName）
     */
    SysUser selectUserById(@Param("id") Long id);

    /**
     * 分页查询用户列表（支持角色、状态、关键字筛选；关联 sys_role 返回 role/roleName）
     */
    IPage<SysUser> selectUserPage(IPage<SysUser> page,
                                  @Param("role") String role,
                                  @Param("status") Integer status,
                                  @Param("keyword") String keyword);
}
