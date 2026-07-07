package com.example.websocket.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 演示入站消息
 *
 * <h2>说明
 * <p>客户端通过 WebSocket 文本帧发送的消息模型，由 DemoMessageService 解析后按 target 字段路由。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoMessage {

    /**
     * 目标会话
     *
     * <h3>字段说明
     * <p>值为 all 时广播给所有活跃会话，否则视为目标会话 ID 定向发送
     */
    private String target;

    /** 消息内容 */
    private String message;
}
