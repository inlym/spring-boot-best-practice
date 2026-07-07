package com.example.websocket.core.support.ws;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Enumeration;
import java.util.Map;

/**
 * WebSocket 握手拦截器，将 HTTP 请求属性传递至 WebSocket 会话
 *
 * <h2>说明
 * <p>在 WebSocket 握手阶段，将 HTTP 请求中由上游组件设置的请求属性
 * 全部复制到 WebSocket 会话的 attributes 中，使其在连接生命周期内可用。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
public class AttributeForwardingInterceptor implements HandshakeInterceptor {

    // ================================ public 方法 ================================

    /**
     * 传递请求属性至会话
     *
     * <h3>处理逻辑
     * <p>遍历 Servlet 请求中的所有属性，逐项复制到 WebSocket 会话的 attributes map 中，
     * 供后续处理器使用。
     *
     * @param request    当前 HTTP 请求
     * @param response   当前 HTTP 响应
     * @param wsHandler  将要处理 WebSocket 消息的处理器
     * @param attributes WebSocket 会话属性 map，握手完成后将传递给会话
     * @return 始终返回 true，允许握手继续
     */
    @Override
    public boolean beforeHandshake(
        @NonNull ServerHttpRequest request,
        @NonNull ServerHttpResponse response,
        @NonNull WebSocketHandler wsHandler,
        @NonNull Map<String, Object> attributes
    ) {
        // 非 Servlet 请求无 Servlet 上下文，无法提取请求属性，跳过转发
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            log.trace("非 Servlet 请求，跳过属性转发，requestType={}", request.getClass().getName());
            return true;
        }

        HttpServletRequest httpRequest = servletRequest.getServletRequest();

        // 遍历 HTTP 请求中的所有属性，逐项复制到 WebSocket 会话属性中
        Enumeration<String> attributeNames = httpRequest.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            attributes.put(name, httpRequest.getAttribute(name));
        }

        return true;
    }

    @Override
    public void afterHandshake(
        @NonNull ServerHttpRequest request,
        @NonNull ServerHttpResponse response,
        @NonNull WebSocketHandler wsHandler,
        @Nullable Exception exception
    ) {
        // 无需在握手完成后执行清理操作
    }
}
