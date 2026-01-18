package com.swimmingsys.common;

/**
 * 返回状态码枚举
 */
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 请求参数错误
     */
    PARAM_ERROR(400, "请求参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * 权限不足
     */
    FORBIDDEN(403, "权限不足"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 服务器内部错误
     */
    ERROR(500, "服务器内部错误"),

    /**
     * 用户名已存在
     */
    USER_EXIST(1001, "用户名已存在"),

    /**
     * 用户名或密码错误
     */
    USER_LOGIN_ERROR(1002, "用户名或密码错误"),

    /**
     * 用户不存在
     */
    USER_NOT_EXIST(1003, "用户不存在"),

    /**
     * 用户已被禁用
     */
    USER_DISABLED(1004, "用户已被禁用"),

    /**
     * 用户已被删除
     */
    USER_DELETED(1005, "用户已被删除");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
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
