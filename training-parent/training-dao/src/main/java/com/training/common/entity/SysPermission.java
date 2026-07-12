package com.training.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限字典表实体
 * <p>对应 sys_permission 表，存储 module:action 格式的 16 个预设权限。</p>
 */
@Data
@TableName("sys_permission")
public class SysPermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 权限编码，如 course:read / course:write */
    private String permCode;

    /** 权限显示名，如 课程查看 / 课程编辑 */
    private String permName;

    /** 权限说明 */
    private String description;

    /** 所属模块分组，用于前端分组展示：course / chapter / question 等 */
    private String module;

    private LocalDateTime createTime;

    /** 逻辑删除：0正常 1已删 */
    @TableLogic
    private Integer deleted;
}
