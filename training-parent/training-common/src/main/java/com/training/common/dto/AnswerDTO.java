package com.training.common.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 单题答案
 */
@Data
public class AnswerDTO {

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    /** 学员答案 */
    private String answer;
}
