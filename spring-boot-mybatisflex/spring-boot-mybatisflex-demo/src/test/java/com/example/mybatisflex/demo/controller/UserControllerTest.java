package com.example.mybatisflex.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户控制器端到端测试
 *
 * <h2>说明
 * <p>通过 MockMvc 验证 MyBatis-Flex 配置在完整 HTTP 链路（请求反序列化 → 控制器 → Service → ORM → 数据库 → 响应序列化）中真实生效。
 * <p>使用 @SpringBootTest 加载完整上下文（含 H2 内存数据库），覆盖增删改查五种典型操作。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    /** MockMvc 测试客户端，由 Spring 注入 */
    @Autowired
    private MockMvc mockMvc;

    // ================================ 测试方法 ================================

    /**
     * 创建用户并回显
     *
     * <h3>验证点
     * <p>接收 JSON 请求体创建用户，响应中包含回显的用户名、邮箱、年龄以及自动生成的主键 ID。
     */
    @Test
    void createAndEchoUser() throws Exception {
        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice\",\"email\":\"alice@example.com\",\"age\":25}")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.email").value("alice@example.com"))
            .andExpect(jsonPath("$.age").value(25));
    }

    /**
     * 按主键查询用户
     *
     * <h3>验证点
     * <p>先创建用户获取自增 ID，再通过 GET /users/{userId} 查询并验证字段一致。
     */
    @Test
    void getByIdReturnsUser() throws Exception {
        // 先创建用户
        String createResult = mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"bob\",\"email\":\"bob@example.com\",\"age\":30}")
        )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // 提取自增 ID 并查询
        int userId = com.jayway.jsonpath.JsonPath.read(createResult, "$.id");

        mockMvc.perform(get("/users/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("bob"))
            .andExpect(jsonPath("$.email").value("bob@example.com"))
            .andExpect(jsonPath("$.age").value(30));
    }

    /**
     * 按用户名查找用户
     *
     * <h3>验证点
     * <p>通过用户名查询返回正确的用户信息。
     */
    @Test
    void findByUsernameReturnsUser() throws Exception {
        // 先创建用户
        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"charlie\",\"email\":\"charlie@example.com\",\"age\":35}")
        )
            .andExpect(status().isOk());

        // 按用户名查询
        mockMvc.perform(
            get("/users/by-username")
                .param("username", "charlie")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("charlie"))
            .andExpect(jsonPath("$.email").value("charlie@example.com"))
            .andExpect(jsonPath("$.age").value(35));
    }

    /**
     * 更新用户邮箱和年龄
     *
     * <h3>验证点
     * <p>通过 PUT /users/{userId} 仅更新邮箱和年龄，用户名保持不变。
     */
    @Test
    void updateEmailAndAge() throws Exception {
        // 先创建用户
        String createResult = mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"dave\",\"email\":\"dave@example.com\",\"age\":40}")
        )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        int userId = com.jayway.jsonpath.JsonPath.read(createResult, "$.id");

        // 更新邮箱和年龄
        mockMvc.perform(
            put("/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"dave.new@example.com\",\"age\":41}")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("dave"))
            .andExpect(jsonPath("$.email").value("dave.new@example.com"))
            .andExpect(jsonPath("$.age").value(41));
    }

    /**
     * 逻辑删除用户
     *
     * <h3>验证点
     * <p>DELETE 请求返回 200，确认逻辑删除操作正常完成（delete_time 被自动填充为当前时间戳）。
     */
    @Test
    void deleteUserLogically() throws Exception {
        // 先创建用户
        String createResult = mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"eve\",\"email\":\"eve@example.com\",\"age\":50}")
        )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        int userId = com.jayway.jsonpath.JsonPath.read(createResult, "$.id");

        // 逻辑删除：delete_time 字段自动填充当前时间戳，数据不会物理删除
        mockMvc.perform(delete("/users/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
