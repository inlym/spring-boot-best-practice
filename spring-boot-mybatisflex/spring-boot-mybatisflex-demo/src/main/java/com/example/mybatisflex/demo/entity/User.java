package com.example.mybatisflex.demo.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 用户信息
 *
 * <h2>说明
 * <p>用于演示 MyBatis-Flex ORM 框架基础操作的实体类，映射到 user_info 表，包含通用字段（id、createTime、updateTime、deleteTime）和业务字段（username、email、age）。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_info")
public class User {

    // ================================ 通用字段 ================================

    /** 主键 ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 创建时间 */
    private Instant createTime;

    /** 更新时间 */
    private Instant updateTime;

    /** 删除时间 */
    @Column(isLogicDelete = true)
    private Instant deleteTime;

    // ================================ 业务字段 ================================

    /**
     * 用户名
     *
     * <h3>数据库字段定义
     * <p>数据类型：varchar(100)
     * <p>非空约束：NOT NULL
     * <p>默认值：无
     */
    private String username;

    /**
     * 邮箱
     *
     * <h3>数据库字段定义
     * <p>数据类型：varchar(200)
     * <p>非空约束：NULL
     * <p>默认值：NULL
     */
    private String email;

    /**
     * 年龄
     *
     * <h3>数据库字段定义
     * <p>数据类型：int unsigned
     * <p>非空约束：NULL
     * <p>默认值：NULL
     */
    private Integer age;
}
