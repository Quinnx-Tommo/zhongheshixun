package com.training.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信小程序登录请求体
 */
@Data
public class WxLoginDTO {

    /** 微信小程序 wx.login 返回的 code */
    @NotBlank(message = "code 不能为空")
    private String code;

    /** 昵称（可选） */
    private String nickName;

    /** 头像 URL（可选） */
    private String avatarUrl;
}
