package com.training.common.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 角色创建/编辑表单 DTO
 */
@Data
public class RoleForm {

    /** 角色编码，如 ADMIN / TEACHER / STUDENT */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过 50")
    private String roleCode;

    /** 角色显示名 */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过 50")
    private String roleName;

    /** 角色说明 */
    @Size(max = 200, message = "描述长度不能超过 200")
    private String description;

    /** 状态：0禁用 1启用 */
    private Integer status = 1;

    /** 关联的权限ID列表 */
    private List<Long> permissionIds;
}
