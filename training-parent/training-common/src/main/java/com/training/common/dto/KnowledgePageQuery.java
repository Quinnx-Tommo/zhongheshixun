package com.training.common.dto;

import lombok.Data;

/**
 * 知识点分页查询
 */
@Data
public class KnowledgePageQuery extends PageQuery {
    /** 课程ID（必传） */
    private Long courseId;
    /** 知识点名称（可选，模糊查询） */
    private String name;
}
