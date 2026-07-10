package com.example.logging.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 日志演示控制器测试
 *
 * <h2>测试说明
 * <p>使用 WebMvcTest 切片测试，验证各日志级别端点均能正常返回 200 状态码和预期响应内容。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@WebMvcTest(LoggingDemoController.class)
class LoggingDemoControllerTest {

    /** MockMvc 测试客户端 */
    @Autowired
    private MockMvc mockMvc;

    // ================================ public 方法 ================================

    /**
     * 测试 TRACE 日志端点
     */
    @Test
    @DisplayName("TRACE 日志端点应正常返回")
    void testTraceLog() throws Exception {
        mockMvc.perform(get("/logs/trace"))
            .andExpect(status().isOk())
            .andExpect(content().string("TRACE 日志已输出"));
    }

    /**
     * 测试 DEBUG 日志端点
     */
    @Test
    @DisplayName("DEBUG 日志端点应正常返回")
    void testDebugLog() throws Exception {
        mockMvc.perform(get("/logs/debug"))
            .andExpect(status().isOk())
            .andExpect(content().string("DEBUG 日志已输出"));
    }

    /**
     * 测试 INFO 日志端点
     */
    @Test
    @DisplayName("INFO 日志端点应正常返回")
    void testInfoLog() throws Exception {
        mockMvc.perform(get("/logs/info"))
            .andExpect(status().isOk())
            .andExpect(content().string("INFO 日志已输出"));
    }

    /**
     * 测试 WARN 日志端点
     */
    @Test
    @DisplayName("WARN 日志端点应正常返回")
    void testWarnLog() throws Exception {
        mockMvc.perform(get("/logs/warn"))
            .andExpect(status().isOk())
            .andExpect(content().string("WARN 日志已输出"));
    }

    /**
     * 测试 ERROR 日志端点
     */
    @Test
    @DisplayName("ERROR 日志端点应正常返回")
    void testErrorLog() throws Exception {
        mockMvc.perform(get("/logs/error"))
            .andExpect(status().isOk())
            .andExpect(content().string("ERROR 日志已输出"));
    }
}
