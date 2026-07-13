package com.training.mapper;

import com.training.common.entity.SysRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /**
     * 分页查询角色列表（支持 roleCode 模糊 + status 精确筛选）
     *
     * @param page     分页对象
     * @param roleCode 角色编码模糊关键字（可空）
     * @param status   状态：0禁用 1启用（可空）
     * @return 分页结果
     */
    IPage<SysRole> selectRolePage(IPage<SysRole> page,
                                  @Param("roleCode") String roleCode,
                                  @Param("status") Integer status);

    /**
     * 查询所有启用状态的角色（下拉框用）
     *
     * @return 启用角色列表
     */
    List<SysRole> selectAllEnabled();
}
