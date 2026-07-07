package com.example.i18n.demo.controller;

import com.example.i18n.core.config.I18nConfig;
import com.example.i18n.core.service.I18nService;
import com.example.i18n.demo.config.DemoMessageSourceBasenameCustomizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 国际化演示控制器端到端测试
 *
 * <h2>说明
 * <p>通过 MockMvc 验证 I18nConfig 与 basename 定制器在完整 HTTP 链路（Accept-Language 解析 → LocaleContextHolder → I18nService → 响应）中真实生效，与配置层单元测试 I18nConfigTest 互补。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@WebMvcTest(I18nDemoController.class)
@Import({I18nConfig.class, I18nService.class, DemoMessageSourceBasenameCustomizer.class})
class I18nDemoControllerTest {

    /** MockMvc 测试客户端，由 Spring 注入 */
    @Autowired
    private MockMvc mockMvc;

    // ================================ 测试方法 ================================

    /**
     * 语言端点在缺省请求头时返回中文
     *
     * <h3>验证配置
     * <p>未发送 Accept-Language，LocaleResolver 回退默认语言 zh-CN，language.current 解析为核心资源文件的简体中文。
     */
    @Test
    void languageEndpointReturnsChineseByDefault() throws Exception {
        mockMvc.perform(get("/messages/language"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("简体中文"));
    }

    /**
     * 语言端点根据英文请求头切换文案
     *
     * <h3>验证配置
     * <p>Accept-Language: en 经语言级回退命中，language.current 解析为核心资源文件的英文。
     */
    @Test
    void languageEndpointReturnsEnglishForEnglishHeader() throws Exception {
        mockMvc.perform(get("/messages/language").header("Accept-Language", "en"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("English"));
    }

    /**
     * 欢迎端点替换占位符并返回中文
     *
     * <h3>验证配置
     * <p>basename 定制器已注册演示模块资源文件，greeting.welcome 的 {0} 占位符替换为 name 参数，并按默认中文返回。
     */
    @Test
    void welcomeEndpointInterpolatesNameInChinese() throws Exception {
        mockMvc.perform(get("/messages/welcome").param("name", "Alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("欢迎，Alice！"));
    }

    /**
     * 欢迎端点根据英文请求头切换文案
     *
     * <h3>验证配置
     * <p>Accept-Language: en 经语言级回退命中，greeting.welcome 按英文返回并完成占位符替换。
     */
    @Test
    void welcomeEndpointReturnsEnglishForEnglishHeader() throws Exception {
        mockMvc.perform(get("/messages/welcome").param("name", "Alice").header("Accept-Language", "en"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Welcome, Alice!"));
    }
}
