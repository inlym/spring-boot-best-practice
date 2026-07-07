package com.example.i18n.demo.config;

import com.example.i18n.core.extension.MessageSourceBasenameCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 演示模块消息源 basename 定制器
 *
 * <h2>说明
 * <p>声明演示模块的多语言资源文件 basename，资源文件位于 classpath:i18n/demo_*.properties，由核心模块的 I18nConfig 自动收集并注册到 MessageSource。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Component
public class DemoMessageSourceBasenameCustomizer implements MessageSourceBasenameCustomizer {

    // ================================ public 方法 ================================

    /**
     * 声明消息源 basename
     *
     * @return 演示模块 basename 列表
     */
    @Override
    public List<String> declareBasenames() {
        return List.of("i18n/demo");
    }
}
