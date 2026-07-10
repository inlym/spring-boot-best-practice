package com.example.logging.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志演示控制器
 *
 * <h2>功能说明
 * <p>通过不同的端点触发各日志级别的输出，演示在 Spring Boot 控制器中使用日志的最佳实践。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
@RestController
public class LoggingDemoController {

    // ================================ public 方法 ================================

    /**
     * 演示 TRACE 级别日志
     *
     * <h3>处理逻辑
     * <p>输出一条 TRACE 级别日志，用于记录方法进入等高频调试信息。
     *
     * @return 操作结果
     */
    @GetMapping("/logs/trace")
    public String traceLog() {
        // 输出 TRACE 级别日志，记录请求进入
        log.trace("TRACE 级别日志 — 用于记录高频调试信息（如方法进入/退出、变量中间值）");
        return "TRACE 日志已输出";
    }

    /**
     * 演示 DEBUG 级别日志
     *
     * <h3>处理逻辑
     * <p>使用参数化方式输出一条 DEBUG 级别日志，展示避免字符串拼接的最佳实践。
     *
     * @return 操作结果
     */
    @GetMapping("/logs/debug")
    public String debugLog() {
        // 输出 DEBUG 级别日志，使用参数化占位符避免不必要字符串拼接
        String username = "demo-user";
        log.debug("DEBUG 级别日志 — 用户 {} 正在执行操作", username);
        return "DEBUG 日志已输出";
    }

    /**
     * 演示 INFO 级别日志
     *
     * <h3>处理逻辑
     * <p>输出一条 INFO 级别日志，用于记录重要的业务操作。
     *
     * @return 操作结果
     */
    @GetMapping("/logs/info")
    public String infoLog() {
        // 输出 INFO 级别日志，记录重要业务操作
        log.info("INFO 级别日志 — 记录重要业务操作，如用户登录、订单创建等");
        return "INFO 日志已输出";
    }

    /**
     * 演示 WARN 级别日志
     *
     * <h3>处理逻辑
     * <p>输出一条 WARN 级别日志，用于记录不影响请求完成的潜在问题。
     *
     * @return 操作结果
     */
    @GetMapping("/logs/warn")
    public String warnLog() {
        // 输出 WARN 级别日志，记录潜在问题
        log.warn("WARN 级别日志 — 记录不影响请求完成的潜在问题，如使用默认配置、功能降级等");
        return "WARN 日志已输出";
    }

    /**
     * 演示 ERROR 级别日志
     *
     * <h3>处理逻辑
     * <p>模拟捕获异常并输出包含异常堆栈的 ERROR 级别日志，然后返回友好的降级结果。
     *
     * @return 操作结果
     */
    @GetMapping("/logs/error")
    public String errorLog() {
        // 演示异常日志打印场景，此处使用 try-catch 捕获异常以展示 log.error 的异常入参用法，不向上传播
        try {
            throw new RuntimeException("模拟业务异常");
        } catch (Exception e) {
            // 将异常作为日志最后一个参数传入，输出完整堆栈
            log.error("ERROR 级别日志 — 记录阻止正常响应的错误", e);
        }

        return "ERROR 日志已输出";
    }
}
