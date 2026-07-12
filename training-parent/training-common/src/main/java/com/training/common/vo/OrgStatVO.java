package com.training.common.vo;

import lombok.Data;

/**
 * 机构维度统计 VO
 *
 * 对应 GET /admin/stats/org
 */
@Data
public class OrgStatVO {

    private String orgName;

    /** 学员数 */
    private Long studentCount;

    /** 累计学习时长（小时） */
    private Double totalStudyHours;

    /** 人均学习时长（小时） */
    private Double avgStudyHours;

    /** 考试通过率（0~100） */
    private Double examPassRate;
}
