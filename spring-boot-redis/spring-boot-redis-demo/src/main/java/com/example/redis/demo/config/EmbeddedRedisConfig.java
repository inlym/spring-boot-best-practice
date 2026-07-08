package com.example.redis.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@Configuration
public class EmbeddedRedisConfig {

    /**
     * 内嵌 Redis 服务器 Bean
     *
     * <h3>端口与绑定
     * <p>绑定 127.0.0.1:6379，与 application.yml 中默认的 spring.data.redis 配置保持一致，
     * 无需额外修改连接参数即可使用。
     *
     * <h3>外部工具访问
     * <p>Redis 客户端工具（redis-cli、RedisInsight 等）可直接通过 localhost:6379 连接。
     *
     * <h3>生命周期
     * <p>应用启动时自动拉起 Redis 进程，应用关闭时通过 destroyMethod 停止进程释放资源。
     *
     * @return 已启动的 Redis 服务器实例，应用关闭时自动停止
     * @throws IOException Redis 进程启动失败时抛出
     */
    @Bean(destroyMethod = "stop")
    public RedisServer redisServer() throws IOException {
        // 构建内嵌 Redis 实例，绑定本地回环地址与默认端口
        RedisServer redisServer = RedisServer.newRedisServer()
                .port(6379)
                .setting("bind 127.0.0.1")
                .build();

        // 启动 Redis 进程，开始监听客户端连接
        redisServer.start();

        return redisServer;
    }
}
