package com.example.clientip.core.config;

import java.util.List;

import com.example.clientip.core.annotation.resolver.ClientIpMethodArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置类
 *
 * <h2>说明
 * <p>注册自定义 HandlerMethodArgumentResolver，扩展控制器方法参数解析能力。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册客户端 IP 参数解析器
     *
     * <h3>配置说明
     * <p>将 ClientIpMethodArgumentResolver 添加至 Spring MVC 参数解析器链，使带有 {@code @ClientIp} 注解的控制器方法参数可自动注入客户端 IP。
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ClientIpMethodArgumentResolver());
    }
}
