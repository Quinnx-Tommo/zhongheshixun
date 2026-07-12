package com.training.common.vo;

import lombok.Data;

/**
 * 登录响应体
 */
@Data
public class LoginVO {
    /** JWT Token */
    private String token;
    /** 用户信息 */
    private Object userInfo;
}
