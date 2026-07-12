package com.training.common.vo;

import lombok.Data;

/**
 * 学习进度响应 VO
 */
@Data
public class StudyProgressVO {

    /** 章节ID */
    private Long chapterId;

    /** 进度百分比 0-100 */
    private Integer progress;

    /** 累计学习时长(秒) */
    private Integer studyDuration;

    /** 上次播放位置(秒) */
    private Integer lastPosition;

    /** 是否已完成 */
    private Integer completed;
}
