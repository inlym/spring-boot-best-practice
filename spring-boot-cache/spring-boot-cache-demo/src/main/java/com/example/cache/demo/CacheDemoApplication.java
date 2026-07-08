package com.example.cache.demo;

import com.example.json.core.config.JacksonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 缓存演示模块启动类
 *
 * <h2>功能说明
 * <p>模块主启动入口，初始化 Spring Boot 应用上下文，演示 Spring Cache 全部缓存注解的典型用法。
 *
 * <h3>组件扫描范围
 * <p>扫描范围设为 com.example.cache，覆盖 core 与 demo 两个包，使 core 模块的 CacheConfig（@Configuration）连同 demo 模块的服务与控制器一并被装配。
 *
 * <h3>Jackson 定制引入
 * <p>json-core 的 JacksonConfig 位于 com.example.json 包、不在本模块扫描范围内，通过 @Import 显式引入，使 JsonMapper 的 Instant 毫秒时间戳等定制在缓存序列化中同样生效。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.example.cache")
@Import(JacksonConfig.class)
public class CacheDemoApplication {

    /**
     * 应用程序主入口方法
     *
     * <h3>处理逻辑
     * <p>启动 Spring Boot 应用程序，初始化应用上下文和所有 Spring 管理的 Bean。
     *
     * @param args 命令行参数
     */
    static void main(String[] args) {
        SpringApplication.run(CacheDemoApplication.class, args);
    }
}
