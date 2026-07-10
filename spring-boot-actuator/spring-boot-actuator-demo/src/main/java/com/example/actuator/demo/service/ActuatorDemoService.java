package com.example.actuator.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Actuator 自定义指标管理服务
 *
 * <h2>功能说明
 * <p>集中创建和管理 Counter、Timer、Gauge、DistributionSummary 四种 Micrometer 指标。
 * <p>所有指标在 Bean 初始化阶段一次性注册，后续请求仅做记录操作，符合 Micrometer 最佳实践。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActuatorDemoService {

    /** Micrometer 指标注册中心，用于创建和管理指标 */
    private final MeterRegistry meterRegistry;

    // ================================ 实例状态字段 ================================

    /** 演示用计数器 */
    private Counter counter;

    /** 演示用计时器 */
    private Timer timer;

    /** 演示用分布摘要 */
    private DistributionSummary distributionSummary;

    /** 模拟队列，用于演示 Gauge 指标 */
    private final List<String> simulatedQueue = new ArrayList<>();

    // ================================ public 方法 ================================

    /**
     * 递增计数器
     *
     * <h3>处理逻辑
     * <p>将 demo.counter 计数 +1，返回递增后的累计值。
     *
     * @return 当前累计计数
     */
    public long incrementCounter() {
        counter.increment();

        return (long) counter.count();
    }

    /**
     * 记录耗时操作
     *
     * <h3>处理逻辑
     * <p>使用 record 方法记录指定操作的执行耗时，自动统计调用次数、总耗时与分布。
     *
     * @param operation 需要计时的操作
     * @return 计时器快照，包含累计调用次数和总耗时，不为 null
     */
    public TimerSnapshot recordTimedOperation(Runnable operation) {
        // 使用 record 方法记录耗时操作的执行时间，自动统计调用次数、总耗时与分布
        timer.record(operation);

        // 返回当前累计统计信息
        return new TimerSnapshot(timer.count(), timer.totalTime(TimeUnit.MILLISECONDS));
    }

    /**
     * 添加队列元素
     *
     * <h3>处理逻辑
     * <p>向模拟队列中添加一个元素，Gauge 指标自动反映变化后的队列大小。
     *
     * @return 当前队列大小
     */
    public int addQueueItem() {
        // 向模拟队列添加元素
        String item = "item-" + System.currentTimeMillis();
        simulatedQueue.add(item);

        log.trace("队列添加元素，当前队列大小={}", simulatedQueue.size());

        return simulatedQueue.size();
    }

    /**
     * 移除队列元素
     *
     * <h3>处理逻辑
     * <p>从模拟队列头部移除一个元素，Gauge 指标自动反映变化后的队列大小。
     * <p>队列为空时跳过移除并返回 skip 结果。
     *
     * @return 队列操作结果，不为 null
     */
    public QueueRemoveResult removeQueueItem() {
        // 队列为空时跳过移除操作
        if (simulatedQueue.isEmpty()) {
            log.trace("队列为空，跳过移除操作");

            return new QueueRemoveResult(0, "remove-skipped");
        }

        // 从队列头部移除一个元素
        simulatedQueue.removeFirst();

        log.trace("队列移除元素，当前队列大小={}", simulatedQueue.size());

        return new QueueRemoveResult(simulatedQueue.size(), "remove");
    }

    /**
     * 记录请求负载大小
     *
     * <h3>处理逻辑
     * <p>通过 DistributionSummary 记录本次请求的负载大小，用于统计分布情况。
     *
     * @param size 请求负载大小
     */
    public void recordRequestSize(double size) {
        // 记录本次请求的负载大小
        distributionSummary.record(size);

        log.trace("记录请求大小，size={}", size);
    }

    // ================================ 初始化方法 ================================

    /**
     * 初始化自定义指标
     *
     * <h3>处理逻辑
     * <p>在 Bean 初始化完成后一次性注册所有演示用指标，避免每次请求重复创建。
     */
    @PostConstruct
    void initMetrics() {
        // 注册 Counter 指标，每次 increment 后自动累加
        counter = Counter.builder("demo.counter")
            .description("演示用计数器，每次请求 +1")
            .register(meterRegistry);

        // 注册 Timer 指标，通过 record 方法记录耗时
        timer = Timer.builder("demo.timer")
            .description("演示用计时器，记录方法执行耗时")
            .register(meterRegistry);

        // 注册 Gauge 指标，每次观测时调用 List::size 获取当前队列大小
        Gauge.builder("demo.queue.size", simulatedQueue, List::size)
            .description("模拟队列当前大小")
            .register(meterRegistry);

        // 注册 DistributionSummary 指标，记录每次请求的负载大小分布
        distributionSummary = DistributionSummary.builder("demo.request.size")
            .description("演示用分布摘要，记录每次请求的负载大小分布")
            .baseUnit("bytes")
            .register(meterRegistry);
    }

    // ================================ 返回模型 ================================

    /**
     * 计时器快照
     *
     * @param totalCount  累计调用次数
     * @param totalTimeMs 累计总耗时（毫秒）
     * @author <a href="https://www.inlym.com">inlym</a>
     * @since 1.0.0
     */
    public record TimerSnapshot(long totalCount, double totalTimeMs) {}

    /**
     * 队列移除操作结果
     *
     * @param queueSize 移除后的队列大小
     * @param action    操作类型（remove：已移除，remove-skipped：队列为空跳过）
     * @author <a href="https://www.inlym.com">inlym</a>
     * @since 1.0.0
     */
    public record QueueRemoveResult(int queueSize, String action) {}
}
