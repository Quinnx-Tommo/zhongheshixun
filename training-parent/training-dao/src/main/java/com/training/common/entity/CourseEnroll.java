package com.training.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程报名实体
 */
@Data
@TableName("course_enroll")
public class CourseEnroll {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学员ID */
    private Long studentId;

    /** 课程ID */
    private Long courseId;

    private LocalDateTime createTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
