package com.example.exception.core.exception;

/**
 * 业务异常基类
 *
 * <h2>类说明
 * <p>作为项目所有业务异常类的基类，提供统一的异常处理能力。
 * <p>异常只负责传递错误消息，错误码和 HTTP 状态码由全局异常处理器统一管理。
 *
 * <h2>设计原则
 * <p>所有自定义业务异常都应继承此类，以确保异常处理的统一性和可维护性。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
public class BaseException extends RuntimeException {

    /**
     * 构造方法
     *
     * @param message 错误消息
     */
    public BaseException(String message) {
        super(message);
    }

    /**
     * 构造方法
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
