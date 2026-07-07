package com.example.i18n.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 国际化配置测试
 *
 * <h2>说明
 * <p>逐项验证 I18nConfig 声明的 MessageSource 与 LocaleResolver 是否真实生效，覆盖中英文资源解析、Accept-Language 命中匹配与白名单外回退默认语言。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@SpringJUnitConfig(I18nConfig.class)
class I18nConfigTest {

    /** 国际化消息源，由 I18nConfig 声明并由 Spring 注入 */
    @Autowired
    private MessageSource messageSource;

    /** 本地化解析器，由 I18nConfig 声明并由 Spring 注入 */
    @Autowired
    private LocaleResolver localeResolver;

    // ================================ 测试方法 ================================

    /**
     * MessageSource 解析中文消息
     *
     * <h3>验证配置
     * <p>核心资源文件 i18n/core_zh 已加载，传入简体中文 Locale 时返回中文文案。
     */
    @Test
    void messageSourceResolvesChineseMessage() {
        String message = messageSource.getMessage("language.current", null, Locale.SIMPLIFIED_CHINESE);

        assertThat(message).isEqualTo("简体中文");
    }

    /**
     * MessageSource 解析英文消息
     *
     * <h3>验证配置
     * <p>核心资源文件 i18n/core_en 已加载，传入英文 Locale 时返回英文文案。
     */
    @Test
    void messageSourceResolvesEnglishMessage() {
        String message = messageSource.getMessage("language.current", null, Locale.ENGLISH);

        assertThat(message).isEqualTo("English");
    }

    /**
     * LocaleResolver 命中白名单内的语言
     *
     * <h3>验证配置
     * <p>AcceptHeaderLocaleResolver 的支持语言列表含 zh-CN，请求头发送 zh-CN 时返回对应 Locale。
     */
    @Test
    void localeResolverMatchesSupportedLanguage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // 模拟客户端发送 Accept-Language: zh-CN
        request.addHeader("Accept-Language", "zh-CN");

        Locale resolved = localeResolver.resolveLocale(request);

        assertThat(resolved).isEqualTo(Locale.forLanguageTag("zh-CN"));
    }

    /**
     * LocaleResolver 经语言级回退匹配裸语言标签
     *
     * <h3>验证配置
     * <p>支持语言列表含纯语言条目 en，请求头发送裸标签 en 时经语言级回退返回 Locale("en")，而非回退默认语言。
     */
    @Test
    void localeResolverMatchesBareLanguageTag() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // 模拟客户端发送裸语言标签 Accept-Language: en
        request.addHeader("Accept-Language", "en");

        Locale resolved = localeResolver.resolveLocale(request);

        assertThat(resolved).isEqualTo(Locale.forLanguageTag("en"));
    }

    /**
     * LocaleResolver 对白名单外的语言回退默认值
     *
     * <h3>验证配置
     * <p>请求未发送 Accept-Language 时，回退到配置的默认语言 zh-CN。
     */
    @Test
    void localeResolverFallsBackToDefaultWhenHeaderAbsent() {
        // 不设置 Accept-Language 请求头，模拟客户端未声明语言偏好
        MockHttpServletRequest request = new MockHttpServletRequest();

        Locale resolved = localeResolver.resolveLocale(request);

        assertThat(resolved).isEqualTo(Locale.forLanguageTag("zh-CN"));
    }
}
