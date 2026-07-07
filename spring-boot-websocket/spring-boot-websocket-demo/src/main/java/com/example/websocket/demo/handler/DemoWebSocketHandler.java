package com.example.websocket.demo.handler;

import com.example.websocket.demo.service.DemoMessageService;
import com.example.websocket.demo.service.DemoWebSocketManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 演示 WebSocket 处理器
 *
 * <h2>功能说明
 * <p>处理演示端点的 WebSocket 连接生命周期事件，将连接建立、关闭委托给会话管理器，将入站文本消息委托给消息服务路由。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoWebSocketHandler extends TextWebSocketHandler {

    /** 演示 WebSocket 会话管理器 */
    private final DemoWebSocketManager demoWebSocketManager;

    /** 演示消息服务 */
    private final DemoMessageService demoMessageService;

    // ================================ public 方法 ================================

    /**
     * WebSocket 连接建立后的处理
     *
     * <h3>处理逻辑
     * <p>将会话添加到会话管理器，后续发送与移除均以会话 ID 为键。
     *
     * @param session WebSocket 会话
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        // 将会话注册到管理器，后续收发与移除均以会话 ID 为键
        demoWebSocketManager.add(session);
    }

    /**
     * 处理接收到的文本消息
     *
     * <h3>处理逻辑
     * <p>记录入站载荷后，将原始文本载荷委托给消息服务解析与路由。
     *
     * @param session WebSocket 会话
     * @param message 文本消息
     */
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        log.trace("收到文本消息，会话 ID：{}，载荷：{}", session.getId(), message.getPayload());

        // 委托消息服务解析载荷并按 target 路由下发
        demoMessageService.handle(message.getPayload());
    }

    /**
     * WebSocket 连接关闭后的处理
     *
     * <h3>处理逻辑
     * <p>记录关闭状态后，从会话管理器移除该会话并释放发送锁。
     *
     * @param session WebSocket 会话
     * @param status  关闭状态
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        log.trace("连接关闭，会话 ID：{}，关闭状态：{}", session.getId(), status);

        // 从管理器移除会话并释放发送锁
        demoWebSocketManager.remove(session.getId());
    }
}
