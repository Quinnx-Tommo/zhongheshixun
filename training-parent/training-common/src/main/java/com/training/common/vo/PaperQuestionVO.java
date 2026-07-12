package com.training.common.vo;

import lombok.Data;

/**
 * 试卷题目VO（返回给学员，不含答案）
 */
@Data
public class PaperQuestionVO {
    private Long id;
    private String title;
    private Integer questionType;
    private String options;
    private Integer score;
}
