package com.example.async.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 自定义异步执行器配置类
 *
 * <h2>说明
 * <p>提供一个基于平台线程池的自定义执行器 Bean，用于演示 @Async 注解指定执行器名称的用法。
 * <p>与核心模块默认的虚拟线程执行器形成对比，展示两种线程策略的差异。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Configuration
public class AsyncDemoExecutorConfig {

    // ================================ public 方法 ================================

    /**
     * 自定义异步执行器 Bean
     *
     * <h3>线程池参数
     * <p>核心线程数为 2，最大线程数为 4，队列容量为 100。
     * <p>线程名前缀为 custom-async-，便于在日志中区分与默认虚拟线程执行器的差异。
     *
     * @return 基于平台线程池的执行器实例
     */
    @Bean("customExecutor")
    public Executor customExecutor() {
        // 创建平台线程池执行器，与默认虚拟线程执行器形成对比
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：空闲时保持的最小线程数
        executor.setCorePoolSize(2);
        // 最大线程数：队列满后最多扩展到的线程数
        executor.setMaxPoolSize(4);
        // 队列容量：核心线程繁忙时任务暂存的队列大小
        executor.setQueueCapacity(100);
        // 线程名前缀，便于在日志中区分执行来源
        executor.setThreadNamePrefix("custom-async-");
        executor.initialize();

        return executor;
    }
}
