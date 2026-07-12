package com.training.common.vo;

import lombok.Data;

/**
 * 学员学习统计 VO（微观）
 *
 * 对应 GET /admin/stats/student
 */
@Data
public class StudentStatVO {

    private Long studentId;
    private String realName;
    private String orgName;
    private String jobType;

    /** 报名课程数 */
    private Long enrollCount;

    /** 累计学习时长（小时） */
    private Double totalStudyHours;

    /** 已完成章节数 */
    private Long completedChapters;

    /** 章节完成率（0~100） */
    private Double completionRate;

    /** 平均考试得分 */
    private Double examAvgScore;
}
