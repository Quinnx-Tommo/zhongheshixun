package com.training.common.constants;

/**
 * 公共常量
 */
public class CommonConstants {

    /** 角色：管理员 */
    public static final String ROLE_ADMIN = "admin";
    /** 角色：讲师 */
    public static final String ROLE_TEACHER = "teacher";
    /** 角色：学员 */
    public static final String ROLE_STUDENT = "student";

    /** 登录失败锁定前缀 */
    public static final String LOGIN_FAIL_PREFIX = "login_fail:";
    /** 最大登录失败次数 */
    public static final int MAX_LOGIN_FAIL = 5;
    /** 登录失败锁定时长（分钟） */
    public static final long LOCK_MINUTES = 15;

    /** JWT Header 前缀 */
    public static final String BEARER_PREFIX = "Bearer ";

    private CommonConstants() {
    }
}
