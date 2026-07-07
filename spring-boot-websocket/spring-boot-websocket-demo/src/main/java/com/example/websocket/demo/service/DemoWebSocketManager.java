package com.example.websocket.demo.service;

import com.example.websocket.core.support.ws.WebSocketManager;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 演示 WebSocket 会话管理器
 *
 * <h2>功能说明
 * <p>继承 {@code WebSocketManager} 的通用会话管理能力，扩展向所有活跃会话广播文本消息的功能。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
@Service
@Validated
public class DemoWebSocketManager extends WebSocketManager {

    // ================================ public 方法 ================================

    /**
     * 向所有活跃会话广播文本消息
     *
     * <h3>处理逻辑
     * <p>获取当前所有活跃会话 ID，逐个委托基类 sendText 串行化发送。
     *
     * @param message 文本消息内容
     */
    public void sendToAll(@NotBlank String message) {
        // 获取所有活跃会话 ID
        List<String> ids = findAllIds();

        // 逐个会话串行化发送
        ids.forEach(id -> sendText(id, message));

        log.trace("广播文本消息完成，目标会话数：{}", ids.size());
    }
}
