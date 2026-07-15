package com.training.common.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 标记章节完成 DTO
 */
@Data
public class CompleteChapterDTO {

    /** 课程ID */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /** 章节ID */
    @NotNull(message = "章节ID不能为空")
    private Long chapterId;
}
