package com.training.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户表单（新增/编辑）
 */
@Data
public class UserForm {
    /** 用户ID（编辑时传） */
    private Long id;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码（新增时必传，编辑时不传则不修改） */
    private String password;

    /** 姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色：admin/teacher/student */
    @NotBlank(message = "角色不能为空")
    private String role;

    /** 头像URL */
    private String avatar;

    /** 所属机构 */
    private String orgName;

    /** 岗位类型：临床/公卫/护理/医技 */
    private String jobType;

    /** 状态：0禁用 1启用 */
    private Integer status;
}
