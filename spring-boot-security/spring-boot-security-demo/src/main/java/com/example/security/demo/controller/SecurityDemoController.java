package com.example.security.demo.controller;

import com.example.security.core.annotation.UserId;
import com.example.security.core.annotation.UserPermission;
import com.example.security.demo.model.UserInfoVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring Security 演示控制器
 *
 * <h2>功能说明
 * <p>演示通过 @UserId 注解在控制器方法中注入用户 ID，并将其作为响应数据返回。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@RestController
public class SecurityDemoController {

    // ================================ public 方法 ================================

    /**
     * 获取当前用户信息
     *
     * <h3>处理逻辑
     * <p>通过 @UserId 注解自动注入由拦截器从请求头提取的用户 ID，封装为响应模型返回。
     *
     * @param userId 用户 ID，由 UserTokenInterceptor 从请求头提取后经 UserIdMethodArgumentResolver 注入
     * @return 用户信息
     */
    @UserPermission
    @GetMapping("/user-info")
    public UserInfoVO getUserInfo(@UserId long userId) {
        // 将注入的用户 ID 封装为响应模型返回
        return UserInfoVO.builder()
            .userId(userId)
            .build();
    }
}
