package com.example.async.demo.controller;

import com.example.async.core.config.AsyncConfig;
import com.example.async.demo.config.AsyncDemoExecutorConfig;
import com.example.async.demo.service.AsyncDemoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

/**
 * 异步演示控制器端到端测试
 *
 * <h2>说明
 * <p>通过 MockMvc 验证 @Async 全部典型用法在完整 HTTP 链路（请求 → 控制器 → 代理增强 → 异步执行 → 响应）中真实生效。
 * <p>重点验证自调用（同步）与代理调用（异步）的行为差异，以及 CompletableFuture 的阻塞等待机制。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@WebMvcTest(AsyncDemoController.class)
@Import({AsyncConfig.class, AsyncDemoService.class, AsyncDemoExecutorConfig.class})
class AsyncDemoControllerTest {

    /** MockMvc 测试客户端，由 Spring 注入 */
    @Autowired
    private MockMvc mockMvc;

    // ================================ 测试方法 ================================

    /**
     * void 异步任务端点立即返回，任务在后台执行中
     *
     * <h3>验证行为
     * <p>请求立即返回不阻塞，任务在后台虚拟线程中运行，响应中 status 已由异步线程更新为 RUNNING。
     * <p>status 为 RUNNING（而非 COMPLETED）证明控制器未等待任务完成。
     */
    @Test
    void submitVoidTaskReturnsImmediatelyTaskRunning() throws Exception {
        mockMvc.perform(post("/async/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taskId").isString())
            .andExpect(jsonPath("$.taskType").value("void"))
            .andExpect(jsonPath("$.status").value("RUNNING"))
            .andExpect(jsonPath("$.submitTime").isString());
    }

    /**
     * CompletableFuture 端点阻塞等待异步结果后返回已完成
     *
     * <h3>验证行为
     * <p>控制器通过 CompletableFuture.get() 阻塞等待异步执行完成，响应中 status 为 COMPLETED 且包含计算结果。
     */
    @Test
    void submitResultTaskBlocksUntilCompleted() throws Exception {
        mockMvc.perform(post("/async/tasks/with-result"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taskId").isString())
            .andExpect(jsonPath("$.taskType").value("result"))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.result").isString())
            .andExpect(jsonPath("$.durationMillis").isNumber());
    }

    /**
     * 自定义执行器端点异步执行，任务在平台线程池中运行
     *
     * <h3>验证行为
     * <p>任务在 customExecutor 平台线程池中异步执行，响应中 status 为 RUNNING 证明控制器立即返回。
     */
    @Test
    void submitCustomExecutorTaskRunsInPlatformThreadPool() throws Exception {
        mockMvc.perform(post("/async/tasks/with-custom-executor"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taskId").isString())
            .andExpect(jsonPath("$.taskType").value("custom-executor"))
            .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    /**
     * 异常任务端点立即返回，异常由全局处理器异步捕获
     *
     * <h3>验证行为
     * <p>控制器不等待异步任务完成即返回，异步线程先更新状态为 RUNNING 后抛出异常。
     * <p>异常由 AsyncUncaughtExceptionHandler 在后台捕获，不影响控制器响应。
     */
    @Test
    void submitErrorTaskReturnsImmediatelyExceptionHandledAsync() throws Exception {
        mockMvc.perform(post("/async/tasks/with-error"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taskId").isString())
            .andExpect(jsonPath("$.taskType").value("error"))
            .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    /**
     * 自调用端点同步阻塞执行，返回已完成状态
     *
     * <h3>验证行为
     * <p>类内部直接调用私有方法不经过 AOP 代理，任务在调用线程同步执行，
     * 响应中 status 为 COMPLETED 且阻塞约 2 秒后才返回。
     */
    @Test
    void selfInvocationExecutesSynchronouslyReturnsCompleted() throws Exception {
        mockMvc.perform(post("/async/tasks/self-invocation"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taskId").isString())
            .andExpect(jsonPath("$.taskType").value("self-invocation"))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.durationMillis").isNumber());
    }

    /**
     * 代理调用端点异步执行，立即返回运行中状态
     *
     * <h3>验证行为
     * <p>通过注入的代理对象调用 @Async 方法，增强正常生效，任务在虚拟线程中异步执行。
     * <p>响应中 status 为 RUNNING（与自调用端点的 COMPLETED 形成对比，证明代理调用真正异步）。
     */
    @Test
    void proxyInvocationExecutesAsynchronouslyReturnsRunning() throws Exception {
        mockMvc.perform(post("/async/tasks/proxy-invocation"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taskId").isString())
            .andExpect(jsonPath("$.taskType").value("proxy-invocation"))
            .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    /**
     * 全量查询端点返回任务记录列表
     *
     * <h3>验证行为
     * <p>查询全部任务记录，响应为 JSON 数组（可为空数组）。
     */
    @Test
    void listTaskRecordsReturnsArray() throws Exception {
        mockMvc.perform(get("/async/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    /**
     * 按 ID 查询端点返回运行中的任务记录
     *
     * <h3>验证行为
     * <p>先提交一个 void 任务，再查询该 taskId，验证返回记录存在且异步线程已更新状态为 RUNNING。
     */
    @Test
    void getTaskRecordReturnsRunningTask() throws Exception {
        // 先提交一个 void 任务，记录返回的 taskId
        String response = mockMvc.perform(post("/async/tasks"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        String taskId = JsonPath.read(response, "$.taskId");

        // 按 taskId 查询，验证异步线程已将状态更新为 RUNNING
        mockMvc.perform(get("/async/tasks/{taskId}", taskId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taskId").value(taskId))
            .andExpect(jsonPath("$.taskType").value("void"))
            .andExpect(jsonPath("$.status").value("RUNNING"));
    }
}
