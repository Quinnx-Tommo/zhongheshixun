package com.training.common.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色-权限树 DTO（返回给前端）
 * <p>用于前端展示某角色当前的权限勾选状态。</p>
 */
@Data
public class RolePermissionDTO {

    /** 角色ID */
    private Long roleId;

    /** 角色编码 */
    private String roleCode;

    /** 角色名 */
    private String roleName;

    /** 当前角色拥有的权限编码列表，如 ["course:read", "course:write"] */
    private List<String> permCodes;

    /** 当前角色拥有的权限ID列表 */
    private List<Long> permIds;
}
