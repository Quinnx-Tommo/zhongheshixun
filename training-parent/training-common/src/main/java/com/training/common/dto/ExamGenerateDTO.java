package com.training.common.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 自动组卷请求
 */
@Data
public class ExamGenerateDTO {

    @NotNull(message = "考试ID不能为空")
    private Long examId;

    @NotNull(message = "知识点ID列表不能为空")
    private List<Long> knowledgePointIds;
}
