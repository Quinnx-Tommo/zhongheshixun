package com.training.common.dto;

import lombok.Data;

/**
 * 讲师分页查询
 */
@Data
public class TeacherPageQuery extends PageQuery {
    /** 讲师姓名（可选，模糊查询） */
    private String realName;
    /** 教学方向（可选，模糊查询） */
    private String direction;
}
