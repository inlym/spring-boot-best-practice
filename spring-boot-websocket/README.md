# spring-boot-websocket

Spring Boot 4 下 WebSocket 的最佳实践模块，聚焦演示 **基于 `WebSocketManager` 的会话生命周期管理与扩展机制**——core
模块提供会话存储、串行化发送、握手属性透传等通用能力，业务模块通过实现
`WebSocketCustomizer` 注册自身端点。

## 模块架构

```
spring-boot-websocket/
├── pom.xml                            # 父 POM，聚合两个子模块
├── spring-boot-websocket-core/        # 核心配置（零业务耦合，可独立复制使用）
└── spring-boot-websocket-demo/        # 演示模块（会话管理 + 入站消息路由）
```

| 子模块                                                         | 定位   | 说明                                                                                                        |
|-------------------------------------------------------------|------|-----------------------------------------------------------------------------------------------------------|
| [`spring-boot-websocket-core`](spring-boot-websocket-core/) | 核心配置 | 含 `WebSocketConfig`、`WebSocketManager` 会话管理基类与 `WebSocketCustomizer` 扩展接口，可原封不动（仅改包名）复制到任意 Spring Boot 项目 |
| [`spring-boot-websocket-demo`](spring-boot-websocket-demo/) | 演示模块 | 完整 Spring Boot 应用，展示 core 配置 + 业务模块注册端点后的会话管理与消息路由                                                        |

## 核心配置

核心逻辑集中在 [
`WebSocketConfig`](spring-boot-websocket-core/src/main/java/com/example/websocket/core/config/WebSocketConfig.java)
一处维护，实现 `WebSocketConfigurer`：

- 启用 WebSocket 功能（`@EnableWebSocket`）
- 自动收集所有 `WebSocketCustomizer` 实现类，在初始化时依次调用，由业务模块自行注册处理器到指定端点

业务模块只需实现 [
`WebSocketCustomizer`](spring-boot-websocket-core/src/main/java/com/example/websocket/core/extension/WebSocketCustomizer.java)
接口，在 `customize` 方法中注册处理器、配置跨域、挂载握手拦截器。

## 会话管理

[
`WebSocketManager`](spring-boot-websocket-core/src/main/java/com/example/websocket/core/support/ws/WebSocketManager.java)
是会话管理的抽象基类，业务模块的 WebSocket 管理器继承此类以复用：

- **会话存储**：以会话 ID 为键的 `ConcurrentHashMap`
- **串行化发送**：每个会话关联独立的 `ReentrantLock`，串行化所有 `sendMessage` 调用，避免并发写入触发底层 socket 的
  `IllegalStateException`
- **会话查询**：`findIdsByAttribute`（按属性匹配）、`findAllIds`（全部活跃会话）、`getById`（按 ID 获取，不存在时抛异常）
- **消息发送**：`sendText`（文本，自动补全时间戳并序列化为 JSON）、`sendBinary`（二进制）

发送类方法返回 `boolean`，会话不存在、已关闭或发送失败时返回 `false`，由调用方根据返回值降级处理。

## 握手属性透传

[
`AttributeForwardingInterceptor`](spring-boot-websocket-core/src/main/java/com/example/websocket/core/support/ws/AttributeForwardingInterceptor.java)
在 WebSocket 握手阶段，将 HTTP 请求中的客户端 IP 复制到 WebSocket 会话的 attributes 中，使其在连接生命周期内可用。

> 本演示模块未配置 IP 过滤器，`clientIp` 属性不会被设置，拦截器在此场景下为空操作；接入 IP 过滤器后自动生效，无需改动。

## 演示端点

演示模块注册了 `/ws/demo` 端点，客户端连接后发送 JSON 文本帧触发消息路由：

| 入站消息体                                 | 行为                   |
|---------------------------------------|----------------------|
| `{"target":"all","message":"hello"}`  | 广播：向所有活跃会话下发 `hello` |
| `{"target":"<会话 ID>","message":"hi"}` | 定向：仅向指定会话 ID 下发 `hi` |

下发给客户端的消息为 JSON 文本帧，格式：

```jsonc
// event 标识消息类型，content 为实际文本载荷，sendTime 由服务端发送时自动补全
{ "event": "demo", "content": "hello", "sendTime": "2026-07-07T12:00:00Z" }
```

## 快速开始

从项目根目录执行：

```bash
mvn spring-boot:run -pl spring-boot-websocket/spring-boot-websocket-demo
```

WebSocket 端点无法用 IntelliJ HTTP Client 直接调试，推荐使用 [`wscat`](https://github.com/websockets/wscat)：

```bash
# 终端 1：建立连接，等待接收消息
wscat -c ws://localhost:8080/ws/demo

# 终端 2：再开一个连接（用于接收广播）
wscat -c ws://localhost:8080/ws/demo

# 终端 3：向任意一个连接发送广播消息，所有连接均会收到
# 在终端 1 或 2 的交互界面输入：
{"target":"all","message":"hello"}

# 定向发送：将 <会话 ID> 替换为终端 1 启动日志中打印的会话 ID（会话已注册，会话 ID：xxx）
{"target":"<会话 ID>","message":"hi"}
```

## 复用 core 模块

1. **直接复制（推荐）**：将 `spring-boot-websocket-core` 中的 `config`、`extension`、`support/ws`、`constant` 包复制到目标项目，改
   `package` 即可生效
2. **Maven 依赖**：在目标项目 `pom.xml` 中声明 `spring-boot-websocket-core` 依赖，启动类扩大组件扫描范围至 core 包以装配
   `@Configuration`

> 推荐直接复制——core 模块不含业务逻辑，复制比引入依赖更轻量，且无版本耦合。
