package com.training.common.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 学习进度上报 DTO
 */
@Data
public class StudyProgressDTO {

    /** 课程ID */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /** 章节ID */
    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    /** 进度百分比 0-100 */
    private Integer progress;

    /** 本次学习时长(秒) */
    private Integer studyDuration;

    /** 上次播放位置(秒) */
    private Integer lastPosition;

    /** 是否已完成 */
    private Boolean completed;
}
