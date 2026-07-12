package com.training.common.dto;

import lombok.Data;

/**
 * 用户分页查询
 */
@Data
public class UserPageQuery extends PageQuery {
    /** 角色：admin/teacher/student */
    private String role;
    /** 关键字（模糊匹配用户名/姓名） */
    private String keyword;
    /** 状态：0禁用 1启用 */
    private Integer status;
}
