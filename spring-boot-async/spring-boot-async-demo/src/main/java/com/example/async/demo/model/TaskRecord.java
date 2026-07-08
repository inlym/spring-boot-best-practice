package com.example.async.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 异步任务执行记录模型
 *
 * <h2>说明
 * <p>记录每次异步方法调用的执行状态与耗时，用于演示 @Async 注解的异步执行效果。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRecord {

    /** 任务 ID */
    private String taskId;

    /** 任务类型（如 void、result、custom-executor、error） */
    private String taskType;

    /** 任务状态（SUBMITTED、RUNNING、COMPLETED、FAILED） */
    private String status;

    /** 执行线程名称（用于观察虚拟线程与平台线程的差异） */
    private String threadName;

    /** 任务结果或错误信息 */
    private String result;

    /** 提交时间 */
    private Instant submitTime;

    /** 开始执行时间 */
    private Instant startTime;

    /** 完成时间 */
    private Instant endTime;

    /** 执行耗时（毫秒） */
    private Long durationMillis;
}
