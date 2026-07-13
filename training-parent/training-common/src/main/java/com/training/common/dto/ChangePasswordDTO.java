package com.training.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修改个人密码 DTO
 * <p>需用户提供原密码验证身份，避免 token 被盗后直接改密。</p>
 */
@Data
public class ChangePasswordDTO {

    /** 原密码 */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /** 新密码（BCrypt 加密后入库） */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度 6-32 位")
    private String newPassword;
}
