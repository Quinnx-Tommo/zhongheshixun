package com.training.common.vo;

import lombok.Data;

import java.util.List;

/**
 * 学员个人学习统计 VO（小程序端）
 *
 * 对应 GET /api/stats/my
 */
@Data
public class MyStatVO {

    private Long studentId;
    private String realName;
    private String orgName;

    /** 累计学习时长（小时） */
    private Double totalStudyHours;

    /** 报名课程数 */
    private Long enrollCount;

    /** 已完成章节数 */
    private Long completedChapters;

    /** 总考试次数 */
    private Long examCount;

    /** 平均考试得分 */
    private Double examAvgScore;

    /** 累计咨询数 */
    private Long consultCount;

    /** 最近 7 日每日学习时长（单位：分钟） */
    private List<DailyHourVO> recent7Days;

    /**
     * 每日学习时长
     *
     * <p>字段命名说明：
     * <ul>
     *   <li>{@code date}：日期（yyyy-MM-dd）</li>
     *   <li>{@code hours}：小时（保留字段，兼容旧前端）</li>
     *   <li>{@code minutes}：分钟（前端 ECharts 真实数据主字段，与 web-student 图表单位一致）</li>
     * </ul>
     * </p>
     */
    @Data
    public static class DailyHourVO {
        private String date;
        /** 学习时长（小时） */
        private Double hours;
        /** 学习时长（分钟），与 hours 同步换算（hours * 60 取整），便于前端 ECharts 直接使用 */
        private Integer minutes;
    }
}
