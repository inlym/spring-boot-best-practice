package com.example.websocket.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WebSocket 演示模块启动类
 *
 * <h2>功能说明
 * <p>模块主启动入口，初始化 Spring Boot 应用上下文，演示会话生命周期管理、入站消息解析与广播/定向路由的最佳实践。
 *
 * <h3>组件扫描范围
 * <p>扫描范围设为 com.example.websocket，覆盖 core 与 demo 两个包，使 core 模块的 WebSocketConfig（@Configuration）连同 demo 模块的定制器、处理器、管理器、服务一并被装配。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.example.websocket")
public class WebSocketDemoApplication {

    /**
     * 应用程序主入口方法
     *
     * <h3>处理逻辑
     * <p>启动 Spring Boot 应用程序，初始化应用上下文和所有 Spring 管理的 Bean。
     *
     * @param args 命令行参数
     */
    static void main(String[] args) {
        SpringApplication.run(WebSocketDemoApplication.class, args);
    }
}
