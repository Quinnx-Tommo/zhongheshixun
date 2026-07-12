package com.training.common.dto;

import lombok.Data;

/**
 * 角色分页查询参数
 */
@Data
public class RoleQuery extends PageQuery {

    /** 角色编码（模糊查询） */
    private String roleCode;

    /** 状态：0禁用 1启用 */
    private Integer status;
}
