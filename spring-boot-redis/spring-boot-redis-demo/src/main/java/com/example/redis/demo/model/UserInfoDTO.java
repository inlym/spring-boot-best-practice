package com.example.redis.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 用户信息 DTO
 *
 * <h2>说明
 * <p>用于演示 Redis 存取过程的对象数据载体，覆盖泛化模板（@class 保留类型）与具化模板（无类型信息）两种序列化方式。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    /**
     * 用户登录名称
     *
     * <h3>字段说明
     * <p>在同一系统内唯一。
     */
    private String username;

    /**
     * 用户年龄
     *
     * <h3>字段说明
     * <p>单位为岁。
     */
    private Integer age;

    /**
     * 用户创建时间
     *
     * <h3>字段说明
     * <p>由 JsonMapper 定制序列化为毫秒时间戳，写入 Redis 与 HTTP 响应保持一致格式。
     */
    private Instant createTime;

    /**
     * 备注信息
     *
     * <h3>字段说明
     * <p>为空时由 NON_NULL 策略过滤，不出现在序列化结果中。
     */
    private String remark;
}
