package com.training.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 管理员重置密码 DTO
 * <p>管理员重置他人密码，无需原密码。</p>
 */
@Data
public class ResetPasswordDTO {

    /** 新密码（BCrypt 加密后入库） */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度 6-32 位")
    private String newPassword;
}
