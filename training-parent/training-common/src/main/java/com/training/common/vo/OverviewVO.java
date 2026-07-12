package com.training.common.vo;

import lombok.Data;

/**
 * 数据概览统计 VO
 *
 * 对应 GET /admin/stats/overview
 */
@Data
public class OverviewVO {

    /** 学员总数 */
    private Long totalStudents;

    /** 课程总数 */
    private Long totalCourses;

    /** 报名人次 */
    private Long totalEnrollments;

    /** 累计学习时长（小时） */
    private Double totalStudyHours;

    /** 考试场次（已提交） */
    private Long totalExamRecords;

    /** 今日活跃学员数 */
    private Long todayActiveStudents;

    /** 咨询总数 / 已回复 / 自动回复率暗示 */
    private Long totalConsults;

    /** 累计学习时长（秒）- 内部字段，由 service 转换为 hours */
    private Long totalStudySeconds;
}
