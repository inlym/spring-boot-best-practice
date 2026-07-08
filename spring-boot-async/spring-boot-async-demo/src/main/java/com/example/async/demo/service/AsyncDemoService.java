package com.example.async.demo.service;

import com.example.async.demo.model.TaskRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 异步方法演示服务
 *
 * <h2>说明
 * <p>演示 @Async 注解的全部典型用法，包括 void 方法、CompletableFuture 返回值、指定执行器名称和异常处理。
 * <p>所有异步任务记录在内存中，可通过查询方法获取执行状态和线程信息。
 *
 * <h2>自调用陷阱
 * <p>{@code executeSelfInvocationDemo} 方法演示了 @Async 的经典自调用陷阱：
 * 在类内部通过 this 调用 @Async 方法不会经过 AOP 代理，方法将在调用线程同步执行。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
@Service
public class AsyncDemoService {

    /** 任务执行记录存储 */
    private final List<TaskRecord> taskRecords = new CopyOnWriteArrayList<>();

    // ================================ public 方法 ================================

    /**
     * 添加任务记录
     *
     * @param record 任务记录
     */
    public void addRecord(TaskRecord record) {
        taskRecords.add(record);
    }

    /**
     * 按任务 ID 查询执行记录
     *
     * @param taskId 任务 ID
     * @return 任务执行记录，不存在时为 null
     */
    public TaskRecord findByTaskId(String taskId) {
        return taskRecords
            .stream()
            .filter(r -> taskId.equals(r.getTaskId()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 查询全部任务执行记录
     *
     * <h3>排序
     * <p>按提交时间倒序排列，最新的记录排在最前。
     *
     * @return 任务执行记录列表
     */
    public List<TaskRecord> listTaskRecords() {
        List<TaskRecord> sorted = new ArrayList<>(taskRecords);
        sorted.sort((a, b) -> b.getSubmitTime().compareTo(a.getSubmitTime()));

        return sorted;
    }

    /**
     * 更新任务记录状态
     *
     * @param taskId 任务 ID
     * @param status 新状态
     * @param result 任务结果或错误信息
     */
    public void updateRecordStatus(String taskId, String status, String result) {
        TaskRecord record = findByTaskId(taskId);
        if (record == null) {
            log.trace("任务记录不存在，跳过状态更新，taskId={}", taskId);
            return;
        }
        record.setStatus(status);
        if (result != null) {
            record.setResult(result);
        }
        // 切换到运行中状态时记录线程名和开始时间，用于观察虚拟线程与平台线程的差异
        if ("RUNNING".equals(status)) {
            record.setStartTime(Instant.now());
            record.setThreadName(Thread.currentThread().getName());
        }
        // 进入终态时记录结束时间和耗时
        if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
            record.setEndTime(Instant.now());
            record.setDurationMillis(
                Duration.between(record.getStartTime(), record.getEndTime()).toMillis()
            );
        }
    }

    /**
     * 执行异步 void 任务（发射即忘模式）
     *
     * <h3>适用场景
     * <p>发送通知、记录日志、触发非关键旁路逻辑等无需返回结果的异步操作。
     *
     * <h3>线程策略
     * <p>使用默认的虚拟线程执行器（AsyncConfig 配置的 SimpleAsyncTaskExecutor）。
     *
     * @param taskId 任务 ID
     */
    @Async
    public void executeVoidTask(String taskId) {
        // 更新状态为执行中，记录当前线程名（应为虚拟线程）
        updateRecordStatus(taskId, "RUNNING", null);

        // 模拟耗时业务处理（如发送邮件、推送通知等）
        simulateWork(2000);

        // 更新状态为已完成
        updateRecordStatus(taskId, "COMPLETED", "void 任务执行完成");
    }

    /**
     * 执行异步任务并返回结果
     *
     * <h3>适用场景
     * <p>需要获取异步计算结果并进行后续组合操作的场景（如多服务并行调用后聚合结果）。
     *
     * <h3>线程策略
     * <p>使用默认的虚拟线程执行器。
     *
     * @param taskId 任务 ID
     * @return 包含任务结果的 CompletableFuture
     */
    @Async
    public CompletableFuture<String> executeResultTask(String taskId) {
        // 更新状态为执行中，记录当前线程名
        updateRecordStatus(taskId, "RUNNING", null);

        // 模拟耗时计算（如调用远程服务、复杂计算等）
        simulateWork(1500);

        String result = String.format("计算结果：[taskId=%s, thread=%s]", taskId, Thread.currentThread().getName());

        // 更新状态为已完成
        updateRecordStatus(taskId, "COMPLETED", result);

        return CompletableFuture.completedFuture(result);
    }

    /**
     * 使用自定义执行器的异步任务
     *
     * <h3>适用场景
     * <p>需要与默认执行器隔离的特定异步任务（如 CPU 密集型任务使用有界线程池限制并发）。
     *
     * <h3>线程策略
     * <p>使用 customExecutor（平台线程池），与默认虚拟线程执行器形成对比。
     *
     * @param taskId 任务 ID
     */
    @Async("customExecutor")
    public void executeCustomExecutorTask(String taskId) {
        // 更新状态为执行中，记录当前线程名（应为 custom-async- 前缀的平台线程）
        updateRecordStatus(taskId, "RUNNING", null);

        // 模拟 CPU 密集型计算
        simulateWork(2000);

        // 更新状态为已完成
        updateRecordStatus(taskId, "COMPLETED", "自定义执行器任务完成");
    }

    /**
     * 执行会抛出异常的异步任务
     *
     * <h3>适用场景
     * <p>演示 AsyncUncaughtExceptionHandler 的全局异常捕获机制。
     * <p>业务模块如需自定义异常处理，应在 @Async 方法内部自行捕获。
     *
     * @param taskId 任务 ID
     */
    @Async
    public void executeErrorTask(String taskId) {
        // 更新状态为执行中
        updateRecordStatus(taskId, "RUNNING", null);

        // 模拟处理过程中发生异常
        simulateWork(500);

        // 业务异常无法恢复，抛出后由全局 AsyncUncaughtExceptionHandler 捕获
        throw new RuntimeException(
            String.format("异步任务执行失败，taskId=%s，请检查业务参数", taskId)
        );
    }

    /**
     * 自调用陷阱演示
     *
     * <h3>陷阱说明
     * <p>在类内部直接调用私有方法执行异步逻辑，不经过 Spring AOP 代理，方法在调用线程同步执行。
     * <p>本端点将同步阻塞约 2 秒后返回，观察记录中的 threadName 与调用方线程一致。
     * <p>正确做法是从外部通过注入的代理对象调用 @Async 方法（如 {@code asyncMethodForSelfInvocation}）。
     *
     * @param taskId 任务 ID
     */
    public void executeSelfInvocationDemo(String taskId) {
        // 直接调用私有方法，不经过 AOP 代理 → 在当前线程同步执行
        // 正确做法：从外部通过注入的代理对象调用 @Async 方法
        doAsyncWork(taskId, "自调用演示完成（同步执行，线程名与调用方一致）");
    }

    /**
     * 通过代理调用的 @Async 方法
     *
     * <h3>说明
     * <p>从外部通过注入的代理对象调用时，@Async 增强生效，方法在虚拟线程中异步执行。
     * <p>对比 {@code executeSelfInvocationDemo}：同一逻辑直接调用（同步）与代理调用（异步）的差异。
     *
     * @param taskId 任务 ID
     */
    @Async
    public void asyncMethodForSelfInvocation(String taskId) {
        // 委托私有方法执行实际逻辑
        doAsyncWork(taskId, "自调用演示完成（异步执行，线程名为虚拟线程）");
    }

    // ================================ private 方法 ================================

    /**
     * 异步任务核心逻辑
     *
     * <h3>说明
     * <p>提取公共逻辑供不同入口方法调用，演示同一段代码在代理调用（异步）与直接调用（同步）下的差异。
     *
     * @param taskId        任务 ID
     * @param resultMessage 完成后的结果描述
     */
    private void doAsyncWork(String taskId, String resultMessage) {
        // 更新状态为执行中，记录当前线程名
        updateRecordStatus(taskId, "RUNNING", null);

        // 模拟耗时处理
        simulateWork(2000);

        // 更新状态为已完成，threadName 揭示执行线程身份
        updateRecordStatus(taskId, "COMPLETED", resultMessage);
    }

    /**
     * 模拟耗时操作
     *
     * <h3>说明
     * <p>使异步任务的执行时间可观测，便于通过 API 查询感受异步与同步的差异。
     *
     * @param millis 模拟耗时（毫秒）
     */
    private void simulateWork(long millis) {
        // Thread.sleep 抛出受检 InterruptedException，不捕获会导致编译失败
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.trace("模拟耗时操作被中断");
        }
    }
}
