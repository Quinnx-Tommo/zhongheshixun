package com.training.common.vo;

import lombok.Data;

import java.util.List;

/**
 * 时间趋势统计 VO
 *
 * 对应 GET /admin/stats/trend
 */
@Data
public class TrendVO {

    /** 时间粒度：day / week / month */
    private String granularity;

    /** 时间序列点列表（按时间排序） */
    private List<TrendPointVO> points;

    @Data
    public static class TrendPointVO {
        /** 时间标签：2026-07-08、2026-W28、2026-07 */
        private String label;

        /** 学习时长（小时） */
        private Double studyHours;

        /** 活跃学员数 */
        private Long activeStudents;

        /** 新增报名数 */
        private Long newEnrollments;
    }
}
