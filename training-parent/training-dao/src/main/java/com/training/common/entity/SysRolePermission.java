package com.training.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色-权限关联表实体
 * <p>对应 sys_role_permission 表，多对多关联角色与权限。</p>
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色ID，FK -> sys_role.id */
    private Long roleId;

    /** 权限ID，FK -> sys_permission.id */
    private Long permissionId;

    private LocalDateTime createTime;
}
