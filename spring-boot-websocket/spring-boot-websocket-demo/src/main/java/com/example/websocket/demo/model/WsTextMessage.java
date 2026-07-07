package com.example.websocket.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * WebSocket 文本消息
 *
 * <h2>说明
 * <p>封装 WebSocket 文本事件消息，包含事件名称、消息内容和发送时间。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsTextMessage {

    /**
     * 事件名称
     *
     * <h3>字段说明
     * <p>标识消息类型，客户端据此区分不同业务消息
     */
    private String event;

    /**
     * 消息内容
     *
     * <h3>字段说明
     * <p>实际下发给客户端的文本载荷
     */
    private String content;

    /**
     * 发送时间
     *
     * <h3>字段说明
     * <p>由调用方在发送前设置，标识消息的产生时刻
     */
    private Instant sendTime;
}
