package com.example.websocket.core.exception;

/**
 * WebSocket 异常
 *
 * <h2>主要用途
 * <p>用于 WebSocket 连接建立、通信过程中出现错误的场景，包括但不限于：
 * <ul>
 *   <li>WebSocket 握手失败</li>
 *   <li>连接超时或中断</li>
 *   <li>消息发送或接收失败</li>
 *   <li>连接状态异常</li>
 * </ul>
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
public class WebSocketException extends RuntimeException {

    /**
     * 构造方法
     *
     * @param message 错误消息
     */
    public WebSocketException(String message) {
        super(message);
    }

    /**
     * 构造方法
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public WebSocketException(String message, Throwable cause) {
        super(message, cause);
    }
}
