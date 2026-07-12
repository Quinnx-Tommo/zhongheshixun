package com.training.common.dto;

import lombok.Data;

/**
 * 课程分页查询参数
 */
@Data
public class CoursePageQuery extends PageQuery {
    /** 课程标题（模糊查询） */
    private String title;

    /** 课程类型：1公开课 2必修课 */
    private Integer courseType;

    /** 状态：0草稿 1已发布 2已下架 */
    private Integer status;
}
