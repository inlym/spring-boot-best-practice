package com.example.websocket.demo.service;

import com.example.websocket.demo.model.WsTextMessage;
import com.example.websocket.demo.model.DemoMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

/**
 * 演示消息服务
 *
 * <h2>服务说明
 * <p>解析 WebSocket 入站文本消息，并按 target 字段路由：值为 all 时广播给所有活跃会话，否则按会话 ID 定向发送。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class DemoMessageService {

    // ================================ 静态常量字段 ================================

    /** 演示消息事件名称，用于标识下发给客户端的消息类型 */
    private static final String DEMO_EVENT = "demo";

    /** 目标为全部会话的广播标识 */
    private static final String ALL_TARGET = "all";

    // ================================ 依赖注入字段 ================================

    /** 演示 WebSocket 会话管理器 */
    private final DemoWebSocketManager demoWebSocketManager;

    /** JSON 序列化映射器，用于解析入站消息 */
    private final JsonMapper jsonMapper;

    // ================================ public 方法 ================================

    /**
     * 处理入站文本消息
     *
     * <h3>处理逻辑
     * <p>将入站 JSON 载荷解析为 DemoMessage，封装为 WsTextMessage 后按 target 路由下发。
     *
     * @param payload 入站文本载荷（JSON 字符串）
     */
    public void handle(@NotBlank String payload) {
        // 解析入站消息
        DemoMessage demoMessage = jsonMapper.readValue(payload, DemoMessage.class);

        // 若消息内容或目标为空，跳过后续处理
        if (
            !StringUtils.hasText(demoMessage.getMessage())
            || !StringUtils.hasText(demoMessage.getTarget())
        ) {
            log.trace("消息内容或目标为空，跳过消息处理，payload={}", payload);
            return;
        }

        // 构建下发给客户端的文本消息并补全发送时间
        WsTextMessage wsMessage = WsTextMessage.builder()
            .event(DEMO_EVENT)
            .content(demoMessage.getMessage())
            .sendTime(Instant.now())
            .build();

        // 序列化为 JSON
        String json = jsonMapper.writeValueAsString(wsMessage);

        // 按 target 路由：all 广播，否则按会话 ID 定向发送
        if (ALL_TARGET.equals(demoMessage.getTarget())) {
            demoWebSocketManager.sendToAll(json);
        } else {
            demoWebSocketManager.sendText(demoMessage.getTarget(), json);
        }
    }
}
