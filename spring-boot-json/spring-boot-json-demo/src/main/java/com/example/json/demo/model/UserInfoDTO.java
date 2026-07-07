package com.example.json.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 用户信息 DTO
 *
 * <h2>说明
 * <p>用于演示控制器层面 JSON 序列化与反序列化效果的数据载体。
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
     * <p>序列化为毫秒时间戳，反序列化时接受毫秒时间戳。
     */
    private Instant createTime;

    /**
     * 备注信息
     *
     * <h3>字段说明
     * <p>为空时由 NON_NULL 策略从响应体中过滤。
     */
    private String remark;
}
