package com.training.common.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 课程报名 DTO
 */
@Data
public class EnrollDTO {

    /** 课程ID */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
}
