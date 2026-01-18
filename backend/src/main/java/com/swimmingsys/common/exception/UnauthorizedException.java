package com.swimmingsys.common.exception;

/**
 * 未授权异常（401）
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("未授权，请先登录");
    }
}
