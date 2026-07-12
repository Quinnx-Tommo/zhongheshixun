package com.training.common.vo;

import lombok.Data;

/**
 * 考试结果VO
 */
@Data
public class ExamResultVO {
    /** 得分 */
    private Integer score;
    /** 总分 */
    private Integer totalScore;
    /** 是否及格 */
    private Boolean passed;
    /** 答对题数 */
    private Integer correctCount;
    /** 答错题数 */
    private Integer wrongCount;
    /** 未答题数 */
    private Integer unansweredCount;
    /** 正确率 */
    private Double correctRate;
}
