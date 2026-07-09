package com.example.exception.demo.config;

import com.example.i18n.core.extension.MessageSourceBasenameCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 异常消息源 basename 定制器
 *
 * <h2>说明
 * <p>声明异常模块的多语言资源文件 basename，资源文件位于 classpath:i18n/exception_*.properties，包含各错误码对应的多语言文案。
 * <p>由核心模块的 I18nConfig 自动收集并注册到 MessageSource，供 ErrorResponseI18nAdvice 在响应写出前替换消息内容。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 4.1.0
 */
@Component
public class ExceptionMessageSourceBasenameCustomizer implements MessageSourceBasenameCustomizer {

    // ================================ public 方法 ================================

    /**
     * 声明消息源 basename
     *
     * @return 异常消息 basename 列表
     */
    @Override
    public List<String> declareBasenames() {
        return List.of("i18n/exception");
    }
}
