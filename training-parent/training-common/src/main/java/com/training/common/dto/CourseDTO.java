package com.training.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 课程新增/编辑 DTO
 */
@Data
public class CourseDTO {

    /** 课程ID（编辑时传） */
    private Long id;

    /** 课程名称 */
    @NotBlank(message = "课程名称不能为空")
    private String title;

    /** 课程描述 */
    private String description;

    /** 封面图URL */
    private String coverUrl;

    /** 讲师ID */
    private Long teacherId;

    /** 类型：1公开课 2必修课 */
    @NotNull(message = "课程类型不能为空")
    private Integer courseType;

    /** 总学时 */
    private Integer totalHours;

    /** 状态：0草稿 1已发布 2已下架 */
    private Integer status;

    /** 是否支持离线学习：0否 1是 */
    private Integer offlineFlag;
}
