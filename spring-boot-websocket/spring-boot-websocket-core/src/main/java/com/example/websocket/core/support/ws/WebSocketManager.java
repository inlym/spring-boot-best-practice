package com.example.websocket.core.support.ws;

import com.example.websocket.core.exception.WebSocketException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * WebSocket 会话管理基类
 *
 * <h2>功能说明
 * <p>提供 WebSocket 会话的通用生命周期管理能力，包括会话的存储、查询、移除和消息发送。
 * <p>每个会话关联独立的 ReentrantLock，串行化所有 sendMessage 调用，避免并发写入触发底层 socket 的 IllegalStateException。
 * 各业务模块的 WebSocket 管理器可继承此类以复用会话管理逻辑。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
@Validated
public abstract class WebSocketManager {

    /**
     * WebSocket 会话映射表
     *
     * <h3>字段说明
     * <p>key 为会话 ID，value 为会话对象
     */
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * 会话发送锁映射表
     *
     * <h3>字段说明
     * <p>key 为会话 ID，value 为该会话的发送锁。
     * <p>用于串行化同一会话的所有 sendMessage 调用，避免并发写入触发底层 socket 的 IllegalStateException。
     */
    private final ConcurrentHashMap<String, ReentrantLock> sendLockMap = new ConcurrentHashMap<>();

    // ================================ public 方法 ================================

    /**
     * 添加 WebSocket 会话到管理器
     *
     * <h3>处理逻辑
     * <p>将会话以其 ID 为键存入映射表，并创建对应的发送锁，记录连接建立日志
     *
     * @param session WebSocket 会话
     */
    public void add(@NotNull WebSocketSession session) {
        // 以会话 ID 为键存入会话表
        String webSocketId = session.getId();
        sessionMap.put(webSocketId, session);

        // 为该会话创建独立的发送锁，串行化后续消息写入
        sendLockMap.computeIfAbsent(webSocketId, _ -> new ReentrantLock());

        log.info("会话已注册，会话 ID：{}，当前会话数：{}", webSocketId, sessionMap.size());
    }

    /**
     * 按会话属性值查找匹配的会话 ID 列表
     *
     * @param key   属性键
     * @param value 目标属性值
     * @return 匹配的会话 ID 列表，无匹配时返回空列表
     */
    public List<String> findIdsByAttribute(@NotBlank String key, @NotNull Object value) {
        // 遍历所有活跃会话，排除已关闭连接，筛选 attributes 中指定键对应值与目标值相等的会话
        return sessionMap.entrySet()
            .stream()
            .filter(entry -> entry.getValue().isOpen())
            .filter(entry -> Objects.equals(entry.getValue().getAttributes().get(key), value))
            .map(Map.Entry::getKey)
            .toList();
    }

    /**
     * 获取所有活跃会话的 ID 列表
     *
     * @return 所有活跃会话 ID 列表，无活跃会话时返回空列表
     */
    public List<String> findAllIds() {
        // 遍历所有会话，筛选仍处于打开状态的会话 ID
        return sessionMap.entrySet()
            .stream()
            .filter(entry -> entry.getValue().isOpen())
            .map(Map.Entry::getKey)
            .toList();
    }

    /**
     * 根据会话 ID 获取 WebSocket 会话
     *
     * <h3>处理逻辑
     * <p>仅在正常流程中会话明确存在时调用，若会话不存在则抛出异常
     *
     * @param webSocketId 会话 ID
     * @return WebSocket 会话
     */
    public WebSocketSession getById(@NotBlank String webSocketId) {
        WebSocketSession session = sessionMap.get(webSocketId);
        if (session == null) {
            throw new WebSocketException(String.format("WebSocket 会话不存在，会话 ID：%s", webSocketId));
        }
        return session;
    }

    /**
     * 根据会话 ID 移除 WebSocket 会话
     *
     * <h3>处理逻辑
     * <p>从映射表中移除指定会话及对应的发送锁，若会话连接未关闭则主动关闭连接，并记录连接关闭日志
     *
     * @param webSocketId 会话 ID
     */
    public void remove(@NotBlank String webSocketId) {
        WebSocketSession removed = sessionMap.remove(webSocketId);
        sendLockMap.remove(webSocketId);

        if (removed == null) {
            return;
        }

        // 若连接未关闭，则主动关闭连接
        // close() 抛出 IOException 受检异常，不捕获会导致编译失败
        // remove 属于清理流程，会话已从映射表移除，close 失败仅记录日志不抛出，
        // 避免异常上抛中断后续清理逻辑；此处采用 fallback 模式（仅记录日志，不中断流程）
        if (removed.isOpen()) {
            try {
                removed.close();
            } catch (IOException e) {
                log.error("关闭 WebSocket 会话失败，会话 ID：{}", webSocketId, e);
            }
        }

        log.trace("会话已注销，会话 ID：{}，当前会话数：{}", webSocketId, sessionMap.size());
    }

    /**
     * 发送 WebSocket 文本消息
     *
     * <h3>处理逻辑
     * <p>通过会话级 lock 串行发送文本帧。
     * <p>会话不存在、已关闭或发送失败时仅记录日志，不向上抛出异常。
     *
     * @param webSocketId 会话 ID
     * @param message     文本消息内容
     */
    public void sendText(@NotBlank String webSocketId, @NotBlank String message) {
        // 构建文本帧并发送
        doSend(webSocketId, new TextMessage(message));
    }

    /**
     * 发送 WebSocket 二进制消息
     *
     * <h3>处理逻辑
     * <p>通过会话级 lock 串行发送二进制帧。
     * <p>会话不存在、已关闭或发送失败时仅记录日志，不向上抛出异常。
     *
     * @param webSocketId 会话 ID
     * @param payload     二进制内容
     */
    public void sendBinary(@NotBlank String webSocketId, @NotNull byte[] payload) {
        // 构建二进制帧并发送
        doSend(webSocketId, new BinaryMessage(payload));
    }

    // ================================ private 方法 ================================

    /**
     * 执行 WebSocket 消息发送
     *
     * <h3>处理逻辑
     * <p>获取会话级 lock 串行化所有 sendMessage 调用。
     * <p>会话不存在时按 TRACE 日志跳过；已关闭时按 WARN 日志跳过；发送抛 IOException 时按 ERROR 日志记录。
     *
     * @param webSocketId 会话 ID
     * @param message     WebSocket 消息
     */
    private void doSend(String webSocketId, WebSocketMessage<?> message) {
        ReentrantLock lock = sendLockMap.computeIfAbsent(webSocketId, _ -> new ReentrantLock());
        lock.lock();
        // 使用 try-finally 保证 lock 释放
        // 原因：lock 持有期间任何异常（如 sendMessage 抛出 RuntimeException）都会跳过 unlock
        // 后果：会话发送锁被永久占用，后续所有发送请求死锁
        try {
            WebSocketSession session = sessionMap.get(webSocketId);
            if (session == null) {
                log.trace("会话不存在，跳过发送，会话 ID：{}", webSocketId);
                return;
            }
            if (!session.isOpen()) {
                log.warn("会话已关闭，跳过发送，会话 ID：{}", webSocketId);
                return;
            }
            // sendMessage 抛出 IOException 受检异常，不捕获会导致编译失败
            // 异常不影响其他会话的消息投递，故采用 fallback 模式：catch 中记录 ERROR 日志，不向上抛出
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                log.error("WebSocket 消息发送失败，会话 ID：{}", webSocketId, e);
            }
        } finally {
            lock.unlock();
        }
    }
}
