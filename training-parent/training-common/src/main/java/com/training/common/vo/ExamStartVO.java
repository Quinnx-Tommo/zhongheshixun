package com.training.common.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 开始考试返回VO
 */
@Data
public class ExamStartVO {
    private Long examId;
    private String title;
    private Integer duration;
    private Integer totalScore;
    private Integer passScore;
    private List<PaperQuestionVO> questions;

    /**
     * 考试开始时间（双保险计时：前端用此值作为倒计时基线，避免客户端时钟漂移）
     * 写入时机：startExam 创建 ExamRecord 时刻
     */
    private LocalDateTime startTime;

    /**
     * 服务端当前时间（双保险：前端可据此同步本地时钟）
     */
    private LocalDateTime serverTime;
}
