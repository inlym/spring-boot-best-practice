package com.example.json.demo.controller;

import com.example.json.core.config.JacksonConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JSON 演示控制器端到端测试
 *
 * <h2>说明
 * <p>通过 MockMvc 验证 JacksonConfig 配置在完整 HTTP 链路（请求反序列化 → 控制器 → 响应序列化）中真实生效，与配置层单元测试 JacksonConfigTest 互补。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@WebMvcTest(JsonDemoController.class)
@Import(JacksonConfig.class)
class JsonDemoControllerTest {

    /** MockMvc 测试客户端，由 Spring 注入 */
    @Autowired
    private MockMvc mockMvc;

    // ================================ 测试方法 ================================

    /**
     * 回显端点在 HTTP 链路应用全部配置
     *
     * <h3>验证配置
     * <p>请求含未知字段 extra 不报错（FAIL_ON_UNKNOWN_PROPERTIES 禁用），createTime 毫秒时间戳完成反序列化与序列化往返（READ/WRITE_NANO 禁用、WRITE_DATES_AS_TIMESTAMPS 启用），null 的 remark 被过滤（NON_NULL）。
     */
    @Test
    void echoAppliesConfigAcrossHttpLayer() throws Exception {
        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice\",\"age\":28,\"createTime\":1700000000000,\"extra\":\"ignored\"}")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.age").value(28))
            .andExpect(jsonPath("$.createTime").value(1700000000000L))
            .andExpect(jsonPath("$.remark").doesNotExist());
    }

    /**
     * 示例端点序列化 Instant 为毫秒时间戳并过滤 null
     *
     * <h3>验证配置
     * <p>响应中 createTime 为毫秒数字（WRITE_DATES_AS_TIMESTAMPS 启用、WRITE_NANO 禁用），null 的 remark 不出现（NON_NULL）。
     */
    @Test
    void sampleSerializesInstantAndFiltersNull() throws Exception {
        mockMvc.perform(get("/users/sample"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.age").value(28))
            .andExpect(jsonPath("$.createTime").isNumber())
            .andExpect(jsonPath("$.remark").doesNotExist());
    }
}
