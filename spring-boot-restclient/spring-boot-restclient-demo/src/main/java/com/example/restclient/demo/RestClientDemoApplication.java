package com.example.restclient.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RestClient 演示模块启动类
 *
 * <h2>功能说明
 * <p>模块主启动入口，初始化 Spring Boot 应用上下文，演示 RestClient 对外发起 HTTP 请求的最佳实践。
 *
 * <h3>组件扫描范围
 * <p>扫描范围设为 com.example.restclient，覆盖 core 与 demo 两个包，使 core 模块的 RestClientConfig（@Configuration）连同 demo 模块的控制器一并被装配。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.example.restclient")
public class RestClientDemoApplication {

    /**
     * 应用程序主入口方法
     *
     * <h3>处理逻辑
     * <p>启动 Spring Boot 应用程序，初始化应用上下文和所有 Spring 管理的 Bean。
     *
     * @param args 命令行参数
     */
    static void main(String[] args) {
        SpringApplication.run(RestClientDemoApplication.class, args);
    }
}
