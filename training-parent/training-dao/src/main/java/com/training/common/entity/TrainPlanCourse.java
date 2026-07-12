package com.training.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 计划关联课程实体
 */
@Data
@TableName("plan_course")
public class TrainPlanCourse {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 计划ID */
    private Long planId;

    /** 课程ID */
    private Long courseId;

    /** 学习顺序 */
    private Integer sortOrder;

    /** 是否必修：0否 1是 */
    private Integer isRequired;

    private LocalDateTime createTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
