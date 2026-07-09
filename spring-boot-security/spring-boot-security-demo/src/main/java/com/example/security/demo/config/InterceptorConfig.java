package com.example.security.demo.config;

import com.example.security.demo.interceptor.UserTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置类
 *
 * <h2>说明
 * <p>注册演示模块自定义拦截器。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    /**
     * 注册用户令牌拦截器
     *
     * <h3>配置说明
     * <p>将 UserTokenInterceptor 添加至拦截器链，在请求进入控制器前从请求头提取用户 ID。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserTokenInterceptor());
    }
}
