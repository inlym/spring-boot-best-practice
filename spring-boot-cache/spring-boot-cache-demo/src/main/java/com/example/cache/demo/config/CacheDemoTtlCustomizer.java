package com.example.cache.demo.config;

import com.example.cache.core.extension.CacheTtlCustomizer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * 缓存 TTL 定制器演示实现
 *
 * <h2>说明
 * <p>演示业务模块如何通过 CacheTtlCustomizer 接口自定义缓存有效期。
 * <p>为 users 缓存设置 5 分钟 TTL，覆盖核心模块默认的 10 分钟。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Component
public class CacheDemoTtlCustomizer implements CacheTtlCustomizer {

    // ================================ 静态常量字段 ================================

    /** 用户缓存名称 */
    public static final String CACHE_NAME_USERS = "users";

    // ================================ public 方法 ================================

    /**
     * 声明缓存 TTL 配置
     *
     * <h3>配置说明
     * <p>为 users 缓存设置 5 分钟有效期，演示业务模块覆盖默认 TTL 的机制。
     *
     * @return 缓存名称与有效期的映射
     */
    @Override
    public Map<String, Duration> declareCacheTtl() {
        // 声明 users 缓存 TTL 为 5 分钟，覆盖默认的 10 分钟
        return Map.of(CACHE_NAME_USERS, Duration.ofMinutes(5));
    }
}
