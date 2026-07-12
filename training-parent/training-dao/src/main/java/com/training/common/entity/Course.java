package com.training.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程实体
 */
@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程名称 */
    private String title;

    /** 课程描述 */
    private String description;

    /** 封面图URL */
    private String coverUrl;

    /** 讲师ID */
    private Long teacherId;

    /** 类型：1公开课 2必修课 */
    private Integer courseType;

    /** 总学时 */
    private Integer totalHours;

    /** 状态：0草稿 1已发布 2已下架 */
    private Integer status;

    /** 是否支持离线学习：0否 1是 */
    private Integer offlineFlag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    /** 讲师姓名（联表 teacher.realName 填充，非持久化） */
    @TableField(exist = false)
    private String teacherName;
}
