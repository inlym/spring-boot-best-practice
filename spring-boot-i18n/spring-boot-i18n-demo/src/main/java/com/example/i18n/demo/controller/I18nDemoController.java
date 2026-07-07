package com.example.i18n.demo.controller;

import com.example.i18n.core.service.I18nService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 国际化演示控制器
 *
 * <h2>功能说明
 * <p>提供演示多语言消息获取的 HTTP 端点，覆盖根据 Accept-Language 请求头切换语言、核心资源文件与业务模块资源文件（经 basename 定制器注册）的消息获取，以及消息占位符参数替换。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
public class I18nDemoController {

    /** 国际化消息服务 */
    private final I18nService i18nService;

    // ================================ public 方法 ================================

    /**
     * 获取当前语言
     *
     * <h3>处理逻辑
     * <p>读取核心模块资源文件的 language.current 消息，返回当前请求命中的语言名称，演示 Accept-Language 请求头切换语言的效果。
     *
     * @return 当前语言名称
     */
    @GetMapping("/messages/language")
    public MessageVO language() {
        // 获取核心模块的当前语言消息，语言由请求上下文解析
        String message = i18nService.getMessage("language.current");

        return MessageVO.builder().message(message).build();
    }

    /**
     * 获取欢迎消息
     *
     * <h3>处理逻辑
     * <p>读取演示模块资源文件的 greeting.welcome 消息，将 name 参数替换到占位符，演示业务模块经 basename 定制器注册资源文件后的消息获取与参数插值。
     *
     * @param name 用户名称，作为占位符替换参数
     * @return 欢迎消息
     */
    @GetMapping("/messages/welcome")
    public MessageVO welcome(@RequestParam(defaultValue = "Guest") String name) {
        // 获取演示模块的欢迎消息，并替换其中的占位符
        String message = i18nService.getMessage("greeting.welcome", new Object[]{name});

        return MessageVO.builder().message(message).build();
    }

    // ================================ 响应模型 ================================

    /** 本地化消息响应模型 */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageVO {

        /** 本地化消息内容 */
        private String message;
    }
}
