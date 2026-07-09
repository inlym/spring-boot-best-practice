package com.example.exception.demo.controller;

import com.example.exception.core.advice.ErrorResponseI18nAdvice;
import com.example.exception.core.config.GlobalExceptionHandler;
import com.example.exception.demo.config.ExceptionMessageSourceBasenameCustomizer;
import com.example.i18n.core.config.I18nConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 异常处理演示控制器端到端测试
 *
 * <h2>说明
 * <p>通过 MockMvc 验证各异常端点返回的 HTTP 状态码与错误响应体，确保全局异常处理器正确拦截各类异常。
 * <p>同时验证 ErrorResponseI18nAdvice 根据 Accept-Language 请求头自动替换多语言错误消息。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@WebMvcTest(ExceptionDemoController.class)
@Import({
    GlobalExceptionHandler.class,
    ErrorResponseI18nAdvice.class,
    I18nConfig.class,
    ExceptionMessageSourceBasenameCustomizer.class
})
class ExceptionDemoControllerTest {

    /** MockMvc 测试客户端，由 Spring 注入 */
    @Autowired
    private MockMvc mockMvc;

    // ================================ 测试方法 ================================

    /**
     * EntityNotFoundException 返回 404 与 ENTITY_NOT_FOUND
     *
     * <h3>验证逻辑
     * <p>触发实体未找到异常，验证 HTTP 404、错误码 ENTITY_NOT_FOUND，且 details 不出现
     */
    @Test
    void entityNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/exceptions/entity-not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ENTITY_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("请求的资源不存在或已被删除"))
            .andExpect(jsonPath("$.details").doesNotExist());
    }

    /**
     * UnpredictableException 返回 500 与 UNPREDICTABLE_ERROR
     *
     * <h3>验证逻辑
     * <p>触发意料之外异常，验证 HTTP 500、错误码 UNPREDICTABLE_ERROR，且 details 不出现
     */
    @Test
    void unpredictableReturns500() throws Exception {
        mockMvc.perform(get("/exceptions/unpredictable"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("UNPREDICTABLE_ERROR"))
            .andExpect(jsonPath("$.message").value("系统处理异常，请稍后重试"))
            .andExpect(jsonPath("$.details").doesNotExist());
    }

    /**
     * BaseException 返回 400 与 BUSINESS_ERROR
     *
     * <h3>验证逻辑
     * <p>触发业务异常基类，验证 HTTP 400、错误码 BUSINESS_ERROR，且 details 不出现
     */
    @Test
    void baseExceptionReturns400() throws Exception {
        mockMvc.perform(get("/exceptions/base"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
            .andExpect(jsonPath("$.message").value("操作失败，请稍后重试"))
            .andExpect(jsonPath("$.details").doesNotExist());
    }

    /**
     * 未预期异常返回 500 与 INTERNAL_ERROR
     *
     * <h3>验证逻辑
     * <p>触发空指针异常，验证由兜底处理器返回 HTTP 500、错误码 INTERNAL_ERROR，消息不泄露内部细节
     */
    @Test
    void genericExceptionReturns500() throws Exception {
        mockMvc.perform(get("/exceptions/generic"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
            .andExpect(jsonPath("$.message").value("服务器繁忙，请稍后重试"))
            .andExpect(jsonPath("$.details").doesNotExist());
    }

    // ================================ i18n 多语言测试 ================================

    /**
     * 英文请求头时 EntityNotFoundException 返回英文消息
     *
     * <h3>验证逻辑
     * <p>发送 Accept-Language: en，验证 ErrorResponseI18nAdvice 根据错误码从英文资源文件查找并替换消息
     */
    @Test
    void entityNotFoundReturnsEnglishMessageForEnglishHeader() throws Exception {
        mockMvc.perform(get("/exceptions/entity-not-found").header("Accept-Language", "en"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ENTITY_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("The requested resource does not exist or has been removed"))
            .andExpect(jsonPath("$.details").doesNotExist());
    }

    /**
     * 缺省请求头时使用默认语言返回中文消息
     *
     * <h3>验证逻辑
     * <p>不发送 Accept-Language，LocaleResolver 回退默认 zh-CN，中文消息与 GlobalExceptionHandler 硬编码一致
     */
    @Test
    void entityNotFoundReturnsChineseMessageByDefault() throws Exception {
        mockMvc.perform(get("/exceptions/entity-not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ENTITY_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("请求的资源不存在或已被删除"))
            .andExpect(jsonPath("$.details").doesNotExist());
    }

    /**
     * 英文请求头时 BaseException 返回英文消息
     *
     * <h3>验证逻辑
     * <p>验证不同错误码（BUSINESS_ERROR）在英文环境下同样正确映射
     */
    @Test
    void baseExceptionReturnsEnglishMessageForEnglishHeader() throws Exception {
        mockMvc.perform(get("/exceptions/base").header("Accept-Language", "en"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
            .andExpect(jsonPath("$.message").value("Operation failed, please try again later"))
            .andExpect(jsonPath("$.details").doesNotExist());
    }
}
