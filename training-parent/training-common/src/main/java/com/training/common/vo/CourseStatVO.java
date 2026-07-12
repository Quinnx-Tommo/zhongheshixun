package com.training.common.vo;

import lombok.Data;

/**
 * 课程统计 VO（宏观）
 *
 * 对应 GET /admin/stats/course
 */
@Data
public class CourseStatVO {

    private Long courseId;
    private String title;
    private String teacherName;

    /** 报名人数 */
    private Long enrollCount;

    /** 正在学习人数（有学习记录） */
    private Long studyCount;

    /** 平均进度（0~100） */
    private Double avgProgress;

    /** 完课率（0~100，进度=100 视为完课） */
    private Double completionRate;
}
