package com.example.redis.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import redis.embedded.RedisServer;

import java.io.IOException;

/**
 * 内嵌 Redis 配置类
 *
 * <h2>配置说明
 * <p>应用启动时自动在本地拉起内嵌 Redis 服务进程，免去手动安装 Redis，实现开箱即用。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 4.1.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class EmbeddedRedisConfig {

    /** 环境变量访问器，用于读取 spring.data.redis.port 等配置属性 */
    private final Environment environment;

    /**
     * 内嵌 Redis 服务器 Bean
     *
     * <h3>端口与绑定
     * <p>端口从 spring.data.redis.port 属性读取，与 application.yml 中的 Redis 连接配置保持一致，
     * 无需额外修改即可使用。绑定地址固定为 127.0.0.1，仅允许本机连接。
     *
     * <h3>外部工具访问
     * <p>Redis 客户端工具（redis-cli、RedisInsight 等）可直接通过 localhost 连接对应端口。
     *
     * <h3>生命周期
     * <p>应用启动时自动拉起 Redis 进程，应用关闭时通过 destroyMethod 与 JVM 关闭钩子双重机制
     * 停止进程释放资源。
     *
     * @return 已启动的 Redis 服务器实例，应用关闭时自动停止
     * @throws IOException Redis 进程启动失败时抛出
     */
    @Bean(destroyMethod = "stop")
    public RedisServer redisServer() throws IOException {
        // 读取 spring.data.redis.port 属性作为内嵌 Redis 端口，未配置时使用默认 6379
        int port = environment.getProperty("spring.data.redis.port", Integer.class, 6379);

        // 构建内嵌 Redis 实例，绑定本地回环地址与从配置读取的端口
        RedisServer redisServer = RedisServer.newRedisServer()
                .port(port)
                .setting("bind 127.0.0.1")
                .build();

        // 启动 Redis 进程，开始监听客户端连接
        redisServer.start();

        log.info("内嵌 Redis 已启动，监听端口：{}", port);

        // 注册 JVM 关闭钩子，确保 Spring 容器未正常关闭时也能终止 Redis 进程
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // JVM 关闭钩子中 stop 失败时无法向上传播，仅记录异常信息
            try {
                redisServer.stop();
            } catch (IOException e) {
                // 关闭钩子内无法使用 Slf4j，异常信息输出到标准错误流
                System.err.println("内嵌 Redis 关闭失败: " + e.getMessage());
            }
        }));

        return redisServer;
    }
}
