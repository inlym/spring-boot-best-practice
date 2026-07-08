package com.example.scheduling.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 定时任务调度演示服务
 *
 * <h2>说明
 * <p>演示 @Scheduled 注解的全部典型用法，包括 fixedRate、fixedDelay、cron、initialDelay 和异常处理。
 * <p>启动应用后在控制台观察各调度类型的日志输出和执行线程名。
 *
 * <h2>调度类型对比
 * <p>fixedRate：以上次调用开始时间为基准，按固定间隔触发，适合独立执行的周期性任务。
 * <p>fixedDelay：以上次调用结束时间为基准，延迟固定时间后触发，适合必须等上次完成的串行任务。
 * <p>cron：基于 Cron 表达式，适合精确时间点执行的业务场景（如每天凌晨 2 点统计报表）。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
@Service
public class SchedulingDemoService {

    // ================================ @Scheduled 方法 ================================

    /**
     * 固定频率定时任务
     *
     * <h3>调度策略
     * <p>每 10 秒执行一次，以上次调用开始时间为基准。
     * <p>SimpleAsyncTaskScheduler 下每次执行创建新虚拟线程，支持并发执行。
     *
     * <h3>适用场景
     * <p>独立执行的周期性任务，如缓存刷新、心跳上报等无需等待上次完成的场景。
     */
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void executeFixedRateTask() {
        log.info("fixedRate 定时任务执行，线程：{}", Thread.currentThread().getName());
    }

    /**
     * 固定延迟定时任务
     *
     * <h3>调度策略
     * <p>每次执行完成后延迟 15 秒再触发下一次。
     * <p>SimpleAsyncTaskScheduler 中 fixed-delay 任务在单线程上串行执行，本次不会与上次并发。
     *
     * <h3>适用场景
     * <p>必须等待上次执行完成的串行任务，如数据迁移、批量处理等依赖执行顺序的场景。
     */
    @Scheduled(fixedDelay = 15, timeUnit = TimeUnit.SECONDS)
    public void executeFixedDelayTask() {
        log.info("fixedDelay 定时任务执行，线程：{}", Thread.currentThread().getName());
    }

    /**
     * Cron 表达式定时任务
     *
     * <h3>调度策略
     * <p>每分钟整点执行一次（cron 表达式：0 * * * * *）。
     * <p>SimpleAsyncTaskScheduler 下每次执行创建新虚拟线程。
     *
     * <h3>适用场景
     * <p>需要精确时间点执行的业务，如每小时报表生成、每天凌晨数据归档。
     */
    @Scheduled(cron = "0 * * * * *")
    public void executeCronTask() {
        log.info("cron 定时任务执行，线程：{}", Thread.currentThread().getName());
    }

    /**
     * 带初始延迟的定时任务
     *
     * <h3>调度策略
     * <p>应用启动 5 秒后首次执行，之后每 30 秒执行一次。
     * <p>initialDelay 确保所有 Bean 初始化完毕后再启动任务。
     *
     * <h3>适用场景
     * <p>需要在应用启动完成后才开始执行的周期性任务，避免与初始化流程抢占资源。
     */
    @Scheduled(initialDelay = 5, fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void executeInitialDelayTask() {
        log.info("initialDelay 定时任务执行（首次延迟 5 秒），线程：{}", Thread.currentThread().getName());
    }

    /**
     * 异常处理演示任务
     *
     * <h3>调度策略
     * <p>每 20 秒执行一次，模拟处理过程中抛出异常。
     *
     * <h3>异常处理
     * <p>异常由 SchedulingConfig 配置的全局 ErrorHandler 捕获并记录日志。
     * <p>异常不影响后续调度，下一次仍按计划触发。
     */
    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    public void executeErrorTask() {
        log.info("error 定时任务开始执行，线程：{}", Thread.currentThread().getName());

        // 模拟业务异常，抛出后由全局 ErrorHandler 捕获，不影响后续调度
        throw new RuntimeException("定时任务演示异常，由全局 ErrorHandler 捕获");
    }
}
