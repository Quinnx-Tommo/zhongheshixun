package com.training.common.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 计划关联课程DTO
 */
@Data
public class PlanCourseAddDTO {
    /** 计划ID */
    @NotNull(message = "计划ID不能为空")
    private Long planId;

    /** 课程ID列表 */
    @NotNull(message = "课程ID列表不能为空")
    private List<Long> courseIds;
}
