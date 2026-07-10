package com.example.actuator.demo.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.example.actuator.demo.service.ActuatorDemoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Actuator 自定义指标演示控制器测试
 *
 * <h2>测试说明
 * <p>使用 WebMvcTest 切片测试，通过 SimpleMeterRegistry 替代真实 MeterRegistry，验证各指标端点均能正常返回 200 状态码和预期响应。
 * <p>Gauge 指标依赖控制器内部共享的 simulatedQueue，使用 DirtiesContext 确保每个测试方法获得独立的上下文。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@WebMvcTest(ActuatorDemoController.class)
@Import(ActuatorDemoService.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class ActuatorDemoControllerTest {

    /** MockMvc 测试客户端 */
    @Autowired
    private MockMvc mockMvc;

    /** JSON 序列化工具 */
    @Autowired
    private ObjectMapper objectMapper;

    // ================================ 测试配置 ================================

    /**
     * 测试用配置，提供 SimpleMeterRegistry 替代真实 MeterRegistry
     */
    @TestConfiguration
    static class TestConfig {

        /**
         * 提供内存版 MeterRegistry，避免 WebMvcTest 切片中缺少 MeterRegistry Bean
         *
         * @return SimpleMeterRegistry 实例
         */
        @Bean
        public MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    // ================================ 测试方法 ================================

    /**
     * 测试 Counter 端点
     */
    @Test
    @DisplayName("Counter 端点应正常返回并累加计数")
    void testCounterDemo() throws Exception {
        // 首次请求
        mockMvc.perform(get("/actuator-demo/counter"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metricName").value("demo.counter"))
            .andExpect(jsonPath("$.metricType").value("Counter"))
            .andExpect(jsonPath("$.currentCount").value(1));

        // 第二次请求，计数应累加到 2
        mockMvc.perform(get("/actuator-demo/counter"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentCount").value(2));
    }

    /**
     * 测试 Timer 端点
     */
    @Test
    @DisplayName("Timer 端点应正常返回并记录耗时")
    void testTimerDemo() throws Exception {
        mockMvc.perform(get("/actuator-demo/timer"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metricName").value("demo.timer"))
            .andExpect(jsonPath("$.metricType").value("Timer"));
    }

    /**
     * 测试 Gauge 添加端点
     */
    @Test
    @DisplayName("Gauge 添加端点应增加队列大小")
    void testGaugeAddDemo() throws Exception {
        mockMvc.perform(get("/actuator-demo/gauge/add"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metricName").value("demo.queue.size"))
            .andExpect(jsonPath("$.metricType").value("Gauge"))
            .andExpect(jsonPath("$.queueSize").value(1))
            .andExpect(jsonPath("$.action").value("add"));

        // 再次添加，队列大小应累加到 2
        mockMvc.perform(get("/actuator-demo/gauge/add"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.queueSize").value(2));
    }

    /**
     * 测试 Gauge 移除端点
     */
    @Test
    @DisplayName("Gauge 移除端点应减少队列大小")
    void testGaugeRemoveDemo() throws Exception {
        // 先添加两个元素
        mockMvc.perform(get("/actuator-demo/gauge/add"));
        mockMvc.perform(get("/actuator-demo/gauge/add"));

        // 移除一个，队列大小应变为 1
        mockMvc.perform(get("/actuator-demo/gauge/remove"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.queueSize").value(1))
            .andExpect(jsonPath("$.action").value("remove"));
    }

    /**
     * 测试 Gauge 移除端点 — 队列为空
     */
    @Test
    @DisplayName("Gauge 移除端点在队列为空时应跳过并返回零")
    void testGaugeRemoveDemoWhenEmpty() throws Exception {
        mockMvc.perform(get("/actuator-demo/gauge/remove"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.queueSize").value(0))
            .andExpect(jsonPath("$.action").value("remove-skipped"));
    }

    /**
     * 测试 DistributionSummary 端点
     */
    @Test
    @DisplayName("DistributionSummary 端点应正常记录负载大小")
    void testDistributionSummaryDemo() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
            ActuatorDemoController.SizeRecordDTO.builder().size(1024).build()
        );

        mockMvc.perform(post("/actuator-demo/distribution-summary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metricName").value("demo.request.size"))
            .andExpect(jsonPath("$.metricType").value("DistributionSummary"))
            .andExpect(jsonPath("$.recordedSize").value(1024));
    }
}
