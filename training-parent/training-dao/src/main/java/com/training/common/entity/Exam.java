package com.training.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试实体
 */
@Data
@TableName("exam")
public class Exam {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 考试名称 */
    private String title;

    /** 考试类型：1课程考试 2计划考试 3单独考试 */
    private Integer examType;

    /** 课程考试关联课程ID */
    private Long courseId;

    /** 计划考试关联计划ID */
    private Long planId;

    /** 总分 */
    private Integer totalScore;

    /** 及格分 */
    private Integer passScore;

    /** 考试时长(分钟) */
    private Integer duration;

    /** 最大重考次数 */
    private Integer maxRetry;

    /** 题目数量 */
    private Integer questionCount;

    /** 预组卷题目ID列表(JSON数组,按courseId关联优先抽题) */
    private String questionIds;

    /** 状态：0草稿 1已发布 */
    private Integer status;

    /** 考试开始时间 */
    private LocalDateTime startTime;

    /** 考试结束时间 */
    private LocalDateTime endTime;

    private LocalDateTime createTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
