package com.example.security.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息响应模型
 *
 * <h2>说明
 * <p>演示通过 @UserId 注解注入用户 ID 后作为响应数据返回。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {

    /** 用户 ID */
    private Long userId;
}
