package com.example.json.demo;

import com.example.json.core.config.JacksonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * JSON 处理演示模块启动类
 *
 * <h2>功能说明
 * <p>模块主启动入口，初始化 Spring Boot 应用上下文，演示控制器层面的 JSON 序列化与反序列化最佳实践。
 * <p>通过 @Import 显式引入 core 模块的 JacksonConfig，演示外部配置的集成方式。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@SpringBootApplication
@Import(JacksonConfig.class)
public class JsonDemoApplication {

    /**
     * 应用程序主入口方法
     *
     * <h3>处理逻辑
     * <p>启动 Spring Boot 应用程序，初始化应用上下文和所有 Spring 管理的 Bean。
     *
     * @param args 命令行参数
     */
    static void main(String[] args) {
        SpringApplication.run(JsonDemoApplication.class, args);
    }
}
