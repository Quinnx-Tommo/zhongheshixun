package com.training.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色字典表实体
 * <p>对应 sys_role 表，存储 ADMIN / TEACHER / STUDENT 等角色字典。</p>
 */
@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码，如 ADMIN / TEACHER / STUDENT */
    private String roleCode;

    /** 角色显示名，如 系统管理员 / 讲师 / 学员 */
    private String roleName;

    /** 角色说明 */
    private String description;

    /** 状态：0禁用 1启用 */
    private Integer status;

    private LocalDateTime createTime;

    /** 逻辑删除：0正常 1已删 */
    @TableLogic
    private Integer deleted;
}
