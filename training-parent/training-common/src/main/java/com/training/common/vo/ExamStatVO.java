package com.training.common.vo;

import lombok.Data;

import java.util.List;

/**
 * 考试统计 VO
 *
 * 对应 GET /admin/stats/exam
 */
@Data
public class ExamStatVO {

    private Long examId;
    private String examTitle;
    private Integer passScore;

    /** 参考人次 */
    private Long participantCount;

    /** 平均分 */
    private Double avgScore;

    /** 最高分 */
    private Integer maxScore;

    /** 最低分 */
    private Integer minScore;

    /** 通过率（0~100） */
    private Double passRate;

    /** 分数段分布：0-59 / 60-69 / 70-79 / 80-89 / 90-100 */
    private List<ScoreRangeVO> scoreRanges;

    @Data
    public static class ScoreRangeVO {
        private String range;
        private Long count;
    }
}
