package com.training.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 培训计划表单（新增/编辑）
 */
@Data
public class TrainPlanForm {
    /** 计划ID（编辑时传） */
    private Long id;

    /** 计划名称 */
    @NotBlank(message = "计划名称不能为空")
    private String title;

    /** 计划描述 */
    private String description;

    /** 状态：0草稿 1已发布 2已结束 */
    private Integer status;
}
