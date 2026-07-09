package com.example.exception.demo;

import com.example.i18n.core.config.I18nConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * 异常处理演示模块启动类
 *
 * <h2>功能说明
 * <p>模块主启动入口，初始化 Spring Boot 应用上下文，演示全局异常处理与统一错误响应的最佳实践。
 *
 * <h3>组件扫描范围
 * <p>扫描范围设为 com.example.exception，覆盖 core 与 demo 两个包，使 core 模块的 GlobalExceptionHandler 连同 demo 模块的控制器一并被装配。
 * <p>通过 @Import 引入 I18nConfig，提供 MessageSource Bean 以激活 ErrorResponseI18nAdvice 的多语言文案替换。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.example.exception")
@Import(I18nConfig.class)
public class ExceptionDemoApplication {

    /**
     * 应用程序主入口方法
     *
     * <h3>处理逻辑
     * <p>启动 Spring Boot 应用程序，初始化应用上下文和所有 Spring 管理的 Bean。
     *
     * @param args 命令行参数
     */
    static void main(String[] args) {
        SpringApplication.run(ExceptionDemoApplication.class, args);
    }
}
