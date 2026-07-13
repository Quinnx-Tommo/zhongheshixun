package com.training.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 权限创建/编辑表单 DTO
 */
@Data
public class PermissionForm {

    /** 权限ID（编辑时必传） */
    private Long id;

    /** 权限编码，如 user:read / course:write */
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过 100")
    private String permCode;

    /** 权限显示名 */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过 100")
    private String permName;

    /** 权限说明 */
    @Size(max = 200, message = "描述长度不能超过 200")
    private String description;

    /** 所属模块：user/course/exam 等 */
    @NotBlank(message = "模块不能为空")
    @Size(max = 50, message = "模块长度不能超过 50")
    private String module;
}
