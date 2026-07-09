package com.example.security.demo.controller;

import com.example.security.core.config.WebMvcConfig;
import com.example.security.demo.config.InterceptorConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring Security 演示控制器端到端测试
 *
 * <h2>说明
 * <p>通过 MockMvc 验证从请求头提取用户 ID → 拦截器取余 → @UserId 参数注入 → 响应返回的完整链路。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@WebMvcTest(SecurityDemoController.class)
@Import({WebMvcConfig.class, InterceptorConfig.class})
class SecurityDemoControllerTest {

    /** MockMvc 测试客户端，由 Spring 注入 */
    @Autowired
    private MockMvc mockMvc;

    // ================================ 测试方法 ================================

    /**
     * 请求头有效值时返回取余后的用户 ID
     *
     * <h3>验证逻辑
     * <p>传入 x-project-user-id: 12345，期望返回 userId=45（12345 % 100）
     */
    @Test
    void getUserInfoReturnsModuloUserId() throws Exception {
        mockMvc.perform(get("/user-info").header("x-project-user-id", "12345"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(45));
    }

    /**
     * 请求头值为 101 时返回用户 ID 为 1
     *
     * <h3>验证逻辑
     * <p>传入 x-project-user-id: 101，取余后为 1，验证余数小于 100 的场景
     */
    @Test
    void getUserInfoReturnsOneFor101() throws Exception {
        mockMvc.perform(get("/user-info").header("x-project-user-id", "101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1));
    }

    /**
     * 请求头值为大数时正确取余
     *
     * <h3>验证逻辑
     * <p>传入 x-project-user-id: 99999，取余后为 99，验证大数值场景
     */
    @Test
    void getUserInfoReturnsModuloForLargeNumber() throws Exception {
        mockMvc.perform(get("/user-info").header("x-project-user-id", "99999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(99));
    }
}
