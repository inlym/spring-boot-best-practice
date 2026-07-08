package com.example.cache.demo.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息模型
 *
 * <h2>说明
 * <p>演示 Spring Cache 注解时的缓存数据载体，模拟从数据库查询到的用户记录。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** 主键 ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 创建时间 */
    private Instant createTime;

    /** 年龄 */
    private Integer age;
}
