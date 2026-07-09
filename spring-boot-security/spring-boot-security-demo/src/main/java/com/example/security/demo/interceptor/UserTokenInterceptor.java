package com.example.security.demo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * 用户令牌拦截器
 *
 * <h2>说明
 * <p>从请求头中提取用户 ID 原始值，经简单运算后写入请求属性和 SecurityContext，供 {@code @UserId} 参数解析器和 {@code @UserPermission} 注解使用。
 *
 * <h2>处理流程
 * <ul>
 *   <li>从请求头获取用户 ID 原始值</li>
 *   <li>将请求头值转为数字，取除以 100 的余数作为用户 ID</li>
 *   <li>将用户 ID 写入请求属性</li>
 *   <li>设置 SecurityContext，授予 ROLE_USER 权限</li>
 * </ul>
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
public class UserTokenInterceptor implements HandlerInterceptor {

    /** 用户 ID 请求头名称 */
    private static final String USER_ID_HEADER = "x-project-user-id";

    // ================================ public 方法 ================================

    /**
     * 请求前置处理
     *
     * <h3>处理逻辑
     * <p>从请求头提取用户 ID 原始值，转为数字后取余 100 作为用户 ID，写入请求属性供后续参数解析器注入，同时设置 SecurityContext 供方法级鉴权使用。
     *
     * @param request  HTTP 请求对象
     * @param response HTTP 响应对象
     * @param handler  处理器对象
     * @return 始终返回 true
     */
    @Override
    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler
    ) {
        // 从请求头获取用户 ID 原始值
        String headerValue = request.getHeader(USER_ID_HEADER);
        if (headerValue == null) {
            log.trace("请求头未携带用户 ID，跳过注入");
            return true;
        }

        // 将请求头值转为数字，取除以 100 的余数作为用户 ID
        long userId = Long.parseLong(headerValue) % 100;

        // 将用户 ID 写入请求属性，供 @UserId 参数解析器使用
        request.setAttribute("userId", userId);

        // 设置 SecurityContext，授予 ROLE_USER 权限，供 @UserPermission 方法级鉴权使用
        SecurityContextHolderStrategy strategy = SecurityContextHolder.getContextHolderStrategy();
        SecurityContext context = strategy.createEmptyContext();
        context.setAuthentication(
            new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
        strategy.setContext(context);

        return true;
    }
}
