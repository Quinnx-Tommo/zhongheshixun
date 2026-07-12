package com.training.mapper;

import com.training.common.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色 Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 按角色编码查询角色
     *
     * @param roleCode 角色编码，如 ADMIN / TEACHER / STUDENT
     * @return 角色实体，未找到返回 null
     */
    SysRole selectByCode(@Param("roleCode") String roleCode);
}
