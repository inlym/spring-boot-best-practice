package com.example.redis.demo;

import com.example.json.core.config.JacksonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Redis 演示模块启动类
 *
 * <h2>功能说明
 * <p>模块主启动入口，初始化 Spring Boot 应用上下文，演示泛化对象、具化对象与二进制三类 RedisTemplate 的存取最佳实践。
 *
 * <h3>组件扫描范围
 * <p>扫描范围设为 com.example.redis，覆盖 core 与 demo 两个包，使 core 模块的 RedisTemplateConfig（@Configuration）与 RedisTemplateService（@Service）连同 demo 模块的服务一并被装配。
 *
 * <h3>Jackson 定制引入
 * <p>json-core 的 JacksonConfig 位于 com.example.json 包、不在本模块扫描范围内，通过 @Import 显式引入，使 JsonMapper 的 Instant 毫秒时间戳等定制在 Redis 序列化中同样生效。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.example.redis")
@Import(JacksonConfig.class)
public class RedisDemoApplication {

    /**
     * 应用程序主入口方法
     *
     * <h3>处理逻辑
     * <p>启动 Spring Boot 应用程序，初始化应用上下文和所有 Spring 管理的 Bean。
     *
     * @param args 命令行参数
     */
    static void main(String[] args) {
        SpringApplication.run(RedisDemoApplication.class, args);
    }
}
