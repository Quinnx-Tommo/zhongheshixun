package com.training.common.dto;

import lombok.Data;

/**
 * 培训计划分页查询
 */
@Data
public class TrainPlanPageQuery extends PageQuery {
    /** 计划名称（可选，模糊查询） */
    private String title;
    /** 状态：0草稿 1已发布 2已结束 */
    private Integer status;
}
