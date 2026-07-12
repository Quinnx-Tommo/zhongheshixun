package com.training.common.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 提交考试请求
 */
@Data
public class ExamSubmitDTO {

    @NotNull(message = "考试ID不能为空")
    private Long examId;

    @NotNull(message = "答案列表不能为空")
    private List<AnswerDTO> answers;
}
