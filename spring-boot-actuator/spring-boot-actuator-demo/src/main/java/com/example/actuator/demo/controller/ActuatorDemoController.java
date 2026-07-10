package com.example.actuator.demo.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import com.example.actuator.demo.service.ActuatorDemoService;
import com.example.actuator.demo.service.ActuatorDemoService.QueueRemoveResult;
import com.example.actuator.demo.service.ActuatorDemoService.TimerSnapshot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Actuator 自定义指标演示控制器
 *
 * <h2>功能说明
 * <p>通过不同的端点演示 Counter、Timer、Gauge、DistributionSummary 四种 Micrometer 指标的使用方式。
 * <p>指标由 ActuatorDemoService 集中管理，控制器仅负责 HTTP 请求处理与响应构建。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class ActuatorDemoController {

    /** 指标管理服务，集中创建和操作 Micrometer 指标 */
    private final ActuatorDemoService actuatorDemoService;

    // ================================ public 方法 ================================

    /**
     * 演示 Counter 指标
     *
     * <h3>处理逻辑
     * <p>每次请求将 demo.counter 计数 +1，表示某个业务事件的发生次数。
     * <p>Counter 为只增不减的单调递增指标，适用于请求计数、错误次数等场景。
     *
     * @return 操作结果，包含当前累计调用次数
     */
    @GetMapping("/actuator-demo/counter")
    public Map<String, Object> counterDemo() {
        // 递增计数器并获取当前累计值
        long count = actuatorDemoService.incrementCounter();

        // 返回当前累计计数
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metricName", "demo.counter");
        result.put("metricType", "Counter");
        result.put("currentCount", count);

        return result;
    }

    /**
     * 演示 Timer 指标
     *
     * <h3>处理逻辑
     * <p>模拟一段耗时操作（随机 50~500ms），使用 record 方法记录执行耗时。
     * <p>Timer 自动统计调用次数、总耗时、最大耗时和耗时分布百分位。
     *
     * @return 操作结果，包含累计调用次数和总耗时
     */
    @GetMapping("/actuator-demo/timer")
    public Map<String, Object> timerDemo() {
        // 记录耗时操作的执行时间
        TimerSnapshot snapshot = actuatorDemoService.recordTimedOperation(() -> {
            // 模拟耗时 50~500ms 的业务操作
            // Thread.sleep() 抛出受检 InterruptedException，不捕获会导致编译失败；捕获后恢复中断标志以遵循线程中断协议
            try {
                Thread.sleep(new Random().nextLong(50, 500));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 返回累计调用次数和总耗时（毫秒）
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metricName", "demo.timer");
        result.put("metricType", "Timer");
        result.put("totalCount", snapshot.totalCount());
        result.put("totalTimeMs", snapshot.totalTimeMs());

        return result;
    }

    /**
     * 演示 Gauge 指标 — 添加队列元素
     *
     * <h3>处理逻辑
     * <p>向模拟队列中添加一个元素，Gauge 指标 demo.queue.size 自动反映变化后的队列大小。
     * <p>Gauge 适用于可增可减的瞬时值，如队列深度、连接池活跃数、缓存条目数等。
     *
     * @return 操作结果，包含当前队列大小
     */
    @GetMapping("/actuator-demo/gauge/add")
    public Map<String, Object> gaugeAddDemo() {
        // 向模拟队列添加元素并获取当前队列大小
        int queueSize = actuatorDemoService.addQueueItem();

        // 返回当前队列大小
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metricName", "demo.queue.size");
        result.put("metricType", "Gauge");
        result.put("queueSize", queueSize);
        result.put("action", "add");

        return result;
    }

    /**
     * 演示 Gauge 指标 — 移除队列元素
     *
     * <h3>处理逻辑
     * <p>从模拟队列头部移除一个元素，Gauge 指标 demo.queue.size 自动反映变化后的队列大小。
     * <p>队列为空时返回零，不抛出异常。
     *
     * @return 操作结果，包含当前队列大小
     */
    @GetMapping("/actuator-demo/gauge/remove")
    public Map<String, Object> gaugeRemoveDemo() {
        // 从模拟队列移除元素并获取操作结果
        QueueRemoveResult removeResult = actuatorDemoService.removeQueueItem();

        // 返回当前队列大小
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metricName", "demo.queue.size");
        result.put("metricType", "Gauge");
        result.put("queueSize", removeResult.queueSize());
        result.put("action", removeResult.action());

        return result;
    }

    /**
     * 演示 DistributionSummary 指标
     *
     * <h3>处理逻辑
     * <p>接收请求体大小值，通过 DistributionSummary 记录其分布。
     * <p>DistributionSummary 适用于记录事件负载大小的分布，如请求体大小、响应体大小、批量处理数量等。
     *
     * @param dto 包含请求体大小值的请求模型
     * @return 操作结果，包含记录的值
     */
    @PostMapping("/actuator-demo/distribution-summary")
    public Map<String, Object> distributionSummaryDemo(@RequestBody @Valid SizeRecordDTO dto) {
        // 记录本次请求的负载大小
        actuatorDemoService.recordRequestSize(dto.getSize());

        // 返回记录结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metricName", "demo.request.size");
        result.put("metricType", "DistributionSummary");
        result.put("recordedSize", dto.getSize());

        return result;
    }

    // ================================ 请求模型 ================================

    /**
     * 分布摘要记录请求模型
     *
     * @author <a href="https://www.inlym.com">inlym</a>
     * @since 1.0.0
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SizeRecordDTO {

        /**
         * 请求负载大小（字节）
         *
         * <h3>字段说明
         * <p>模拟请求体大小的数值，用于演示 DistributionSummary 的负载分布记录能力。
         */
        @NotNull
        @Min(1)
        @Max(10485760)
        private Integer size;
    }
}
