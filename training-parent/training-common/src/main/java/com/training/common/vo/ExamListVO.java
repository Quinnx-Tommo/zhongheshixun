package com.training.common.vo;

import lombok.Data;

/**
 * 学员维度考试列表 VO
 *
 * 字段分两类：
 * 1. 考试本身字段（来自 exam 表）
 * 2. 学员维度字段（通过当前 userId 计算得出）
 *
 * 屏蔽 deleted 等敏感字段，不暴露 Exam 实体。
 */
@Data
public class ExamListVO {

    /** 考试ID */
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

    // ========== 学员维度字段（通过当前 userId 算出来） ==========

    /**
     * 学员维度状态：
     * 0 未开始（exam_record 里没记录）
     * 1 已完成（已提交等批阅，record.status = 1）
     * 2 已批阅（record.status = 2）
     */
    private Integer status;

    /** 答对题数（已批阅时才有） */
    private Integer correctCount;

    /** 答错题数（已批阅时才有） */
    private Integer wrongCount;

    /** 最终分数（已批阅时才有） */
    private Integer score;

    /** 是否通过（已批阅时才有） */
    private Boolean passed;

    /** 已参加次数（该 exam 的 record 数） */
    private Integer times;

    /** 剩余重考次数 */
    private Integer retryLeft;

    /** 最近一条考试记录 ID（查看成绩用，未开始时为 null） */
    private Long recordId;
}
