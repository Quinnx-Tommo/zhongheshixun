package com.training.common.dto;

import lombok.Data;

/**
 * 权限分页查询参数
 */
@Data
public class PermissionQuery extends PageQuery {

    /** 模块名（精确匹配，可空） */
    private String module;

    /** 权限编码模糊关键字（可空） */
    private String permCode;
}
