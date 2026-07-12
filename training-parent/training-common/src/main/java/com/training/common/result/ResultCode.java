package com.training.common.result;

/**
 * 响应码枚举
 */
public enum ResultCode {
    /** 成功 */
    SUCCESS(200, "success"),
    /** 参数错误 */
    PARAM_ERROR(400, "参数错误"),
    /** 未登录或登录已过期 */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    /** 无权限 */
    FORBIDDEN(403, "无权限"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 服务端错误 */
    SERVER_ERROR(500, "系统繁忙，请稍后重试"),

    /** 业务异常 */
    BUSINESS_ERROR(1000, "业务异常"),
    /** 用户名已存在 */
    USERNAME_EXISTS(1001, "用户名已存在"),
    /** 用户名或密码错误 */
    LOGIN_FAIL(1002, "用户名或密码错误"),
    /** 课程不存在 */
    COURSE_NOT_FOUND(1003, "课程不存在"),
    /** 考试不存在 */
    EXAM_NOT_FOUND(1004, "考试不存在"),
    /** 已报名该课程 */
    ENROLL_EXISTS(1005, "已报名该课程"),
    /** 考试时间已结束 */
    EXAM_TIME_OVER(1006, "考试时间已结束"),
    /** 已达到最大重考次数 */
    RETRY_LIMIT(1007, "已达到最大重考次数");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
