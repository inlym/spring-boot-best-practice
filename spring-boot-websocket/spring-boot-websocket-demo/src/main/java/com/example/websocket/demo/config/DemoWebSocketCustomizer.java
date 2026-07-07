package com.example.websocket.demo.config;

import com.example.websocket.core.extension.WebSocketCustomizer;
import com.example.websocket.core.support.ws.AttributeForwardingInterceptor;
import com.example.websocket.demo.handler.DemoWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 演示定制器
 *
 * <h2>功能说明
 * <p>将演示 WebSocket 处理器注册到 /ws/demo 端点。
 * <p>允许所有来源的跨域请求。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class DemoWebSocketCustomizer implements WebSocketCustomizer {

    /** 演示 WebSocket 处理器 */
    private final DemoWebSocketHandler demoWebSocketHandler;

    // ================================ public 方法 ================================

    /**
     * 定制 WebSocket 处理器注册配置
     *
     * <h3>配置说明
     * <p>将演示处理器注册到 /ws/demo 端点
     * <p>允许所有来源的跨域请求
     * <p>注册握手拦截器，将 HTTP 请求属性传递至 WebSocket 会话
     *
     * @param registry WebSocket 处理器注册表
     */
    @Override
    public void customize(WebSocketHandlerRegistry registry) {
        registry
            .addHandler(demoWebSocketHandler, "/ws/demo")
            .setAllowedOrigins("*")
            .addInterceptors(new AttributeForwardingInterceptor());
    }
}
