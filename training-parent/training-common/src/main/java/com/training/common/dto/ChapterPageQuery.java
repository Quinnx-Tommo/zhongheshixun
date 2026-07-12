package com.training.common.dto;

import lombok.Data;

/**
 * 章节分页查询
 */
@Data
public class ChapterPageQuery extends PageQuery {
    /** 课程ID（必传） */
    private Long courseId;
    /** 章节标题（可选，模糊查询） */
    private String title;
}
