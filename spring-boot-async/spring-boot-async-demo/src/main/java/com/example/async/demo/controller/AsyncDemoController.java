package com.example.async.demo.controller;

import com.example.async.demo.model.TaskRecord;
import com.example.async.demo.service.AsyncDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 异步方法演示控制器
 *
 * <h2>功能说明
 * <p>提供演示 @Async 注解全部典型用法的 HTTP 端点，覆盖 void 方法、CompletableFuture 返回值、
 * 指定执行器名称、异常处理、自调用陷阱和代理调用对比。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Validated
public class AsyncDemoController {

    /** 异步演示服务 */
    private final AsyncDemoService asyncDemoService;

    // ================================ public 方法 ================================

    /**
     * 提交异步 void 任务
     *
     * <h3>异步行为
     * <p>请求立即返回，任务在后台虚拟线程中执行。
     * <p>可通过 GET /async/tasks/{taskId} 跟踪任务执行状态。
     *
     * @return 任务记录（状态为 SUBMITTED）
     */
    @PostMapping("/async/tasks")
    public TaskRecord submitVoidTask() {
        // 生成唯一任务 ID
        String taskId = UUID.randomUUID().toString();

        // 创建任务记录并写入内存
        TaskRecord record = TaskRecord
            .builder()
            .taskId(taskId)
            .taskType("void")
            .status("SUBMITTED")
            .submitTime(Instant.now())
            .build();
        asyncDemoService.addRecord(record);

        // 通过代理调用 @Async 方法，任务在后台虚拟线程中执行
        asyncDemoService.executeVoidTask(taskId);

        return record;
    }

    /**
     * 提交异步任务并等待结果
     *
     * <h3>异步行为
     * <p>通过 CompletableFuture 获取异步执行结果，请求阻塞等待直到任务完成。
     * <p>超时时间为 10 秒，超时后返回当前记录状态。
     *
     * @return 任务记录（状态为 COMPLETED）
     */
    @PostMapping("/async/tasks/with-result")
    public TaskRecord submitResultTask() throws ExecutionException, InterruptedException, TimeoutException {
        // 生成唯一任务 ID
        String taskId = UUID.randomUUID().toString();

        // 创建任务记录并写入内存
        TaskRecord record = TaskRecord
            .builder()
            .taskId(taskId)
            .taskType("result")
            .status("SUBMITTED")
            .submitTime(Instant.now())
            .build();
        asyncDemoService.addRecord(record);

        // 通过代理调用 @Async 方法，返回 CompletableFuture
        CompletableFuture<String> future = asyncDemoService.executeResultTask(taskId);

        // 阻塞等待异步结果，最长等待 10 秒
        future.get(10, TimeUnit.SECONDS);

        // 返回最新的任务记录
        return asyncDemoService.findByTaskId(taskId);
    }

    /**
     * 使用自定义执行器的异步任务
     *
     * <h3>异步行为
     * <p>任务在 customExecutor（平台线程池）中执行，与默认虚拟线程执行器形成对比。
     * <p>观察返回记录中的 threadName，应以 custom-async- 开头。
     *
     * @return 任务记录（状态为 SUBMITTED）
     */
    @PostMapping("/async/tasks/with-custom-executor")
    public TaskRecord submitCustomExecutorTask() {
        // 生成唯一任务 ID
        String taskId = UUID.randomUUID().toString();

        // 创建任务记录并写入内存
        TaskRecord record = TaskRecord
            .builder()
            .taskId(taskId)
            .taskType("custom-executor")
            .status("SUBMITTED")
            .submitTime(Instant.now())
            .build();
        asyncDemoService.addRecord(record);

        // 通过代理调用指定 customExecutor 的 @Async 方法
        asyncDemoService.executeCustomExecutorTask(taskId);

        return record;
    }

    /**
     * 提交会产生异常的异步任务
     *
     * <h3>异步行为
     * <p>任务在后台执行时抛出 RuntimeException，由全局 AsyncUncaughtExceptionHandler 捕获并记录日志。
     * <p>任务记录状态将停留在 RUNNING（异常在代理层被拦截，无法更新记录状态为 FAILED）。
     *
     * @return 任务记录（状态为 SUBMITTED）
     */
    @PostMapping("/async/tasks/with-error")
    public TaskRecord submitErrorTask() {
        // 生成唯一任务 ID
        String taskId = UUID.randomUUID().toString();

        // 创建任务记录并写入内存
        TaskRecord record = TaskRecord
            .builder()
            .taskId(taskId)
            .taskType("error")
            .status("SUBMITTED")
            .submitTime(Instant.now())
            .build();
        asyncDemoService.addRecord(record);

        // 通过代理调用会抛出异常的 @Async 方法
        asyncDemoService.executeErrorTask(taskId);

        return record;
    }

    /**
     * 提交自调用陷阱演示任务
     *
     * <h3>陷阱说明
     * <p>通过 this 在类内部调用 @Async 方法不经过 AOP 代理，方法在调用线程同步执行。
     * <p>本端点将阻塞约 2 秒后才返回，证明自调用绕过了 @Async 增强。
     * <p>观察记录中的 threadName 与 callerThreadName。
     *
     * @return 任务记录（包含调用线程名和执行线程名）
     */
    @PostMapping("/async/tasks/self-invocation")
    public TaskRecord submitSelfInvocationDemo() {
        // 生成唯一任务 ID
        String taskId = UUID.randomUUID().toString();

        // 创建任务记录并写入内存，记录调用方线程名
        String callerThreadName = Thread.currentThread().getName();
        TaskRecord record = TaskRecord
            .builder()
            .taskId(taskId)
            .taskType("self-invocation")
            .status("SUBMITTED")
            .threadName("caller: " + callerThreadName)
            .submitTime(Instant.now())
            .build();
        asyncDemoService.addRecord(record);

        // 调用自调用演示方法（将同步执行，阻塞约 2 秒）
        asyncDemoService.executeSelfInvocationDemo(taskId);

        // 返回最新的任务记录
        return asyncDemoService.findByTaskId(taskId);
    }

    /**
     * 提交代理调用演示任务（正确方式）
     *
     * <h3>对比说明
     * <p>与自调用陷阱端点（/async/tasks/self-invocation）形成对比：
     * 本端点通过注入的代理对象调用 @Async 方法，@Async 增强正常生效，方法在虚拟线程中异步执行。
     * <p>请求立即返回，任务记录中的 threadName 应为虚拟线程名，与调用方线程不同。
     *
     * @return 任务记录（状态为 SUBMITTED）
     */
    @PostMapping("/async/tasks/proxy-invocation")
    public TaskRecord submitProxyInvocationDemo() {
        // 生成唯一任务 ID
        String taskId = UUID.randomUUID().toString();

        // 创建任务记录并写入内存
        TaskRecord record = TaskRecord
            .builder()
            .taskId(taskId)
            .taskType("proxy-invocation")
            .status("SUBMITTED")
            .submitTime(Instant.now())
            .build();
        asyncDemoService.addRecord(record);

        // 通过代理调用 @Async 方法，异步增强正常生效 → 虚拟线程中执行
        asyncDemoService.asyncMethodForSelfInvocation(taskId);

        return record;
    }

    /**
     * 按任务 ID 查询执行记录
     *
     * @param taskId 任务 ID
     * @return 任务执行记录，不存在时为 null
     */
    @GetMapping("/async/tasks/{taskId}")
    public TaskRecord getTaskRecord(@PathVariable String taskId) {
        // 从内存中查询任务记录
        return asyncDemoService.findByTaskId(taskId);
    }

    /**
     * 查询全部任务执行记录
     *
     * <h3>排序
     * <p>按提交时间倒序排列，最新提交的任务排在最前。
     *
     * @return 任务执行记录列表
     */
    @GetMapping("/async/tasks")
    public List<TaskRecord> listTaskRecords() {
        // 从内存中查询全部记录
        return asyncDemoService.listTaskRecords();
    }
}
