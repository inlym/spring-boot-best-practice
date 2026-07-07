package com.example.i18n.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 国际化演示模块启动类
 *
 * <h2>功能说明
 * <p>模块主启动入口，初始化 Spring Boot 应用上下文，演示国际化配置在 HTTP 层的多语言消息获取最佳实践。
 *
 * <h3>组件扫描范围
 * <p>扫描范围设为 com.example.i18n，覆盖 core 与 demo 两个包，使 core 模块的 I18nConfig（@Configuration）与 I18nService（@Service）连同 demo 模块的定制器一并被装配。
 * <p>core 模块除配置类外还含服务类，无法仅靠 @Import 引入，故采用扩大组件扫描范围的方式装配。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.example.i18n")
public class I18nDemoApplication {

    /**
     * 应用程序主入口方法
     *
     * <h3>处理逻辑
     * <p>启动 Spring Boot 应用程序，初始化应用上下文和所有 Spring 管理的 Bean。
     *
     * @param args 命令行参数
     */
    static void main(String[] args) {
        SpringApplication.run(I18nDemoApplication.class, args);
    }
}
