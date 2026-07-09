package com.example.clientip.core.config;

import com.example.clientip.core.support.ip.ClientIpFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Servlet 过滤器配置类
 *
 * <h2>说明
 * <p>集中注册 Servlet 层过滤器实例，统一管理客户端 IP 提取相关的过滤器 Bean。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Configuration
public class FilterConfig {

    /**
     * 注册 X-Forwarded-For 客户端 IP 过滤器
     *
     * <h3>配置说明
     * <p>从 X-Forwarded-For 请求头按逗号分割取倒数第二段作为客户端 IP。
     *
     * @return 客户端 IP 过滤器注册 Bean
     */
    @Bean
    public FilterRegistrationBean<ClientIpFilter> xForwardedForFilter() {
        ClientIpFilter filter = new ClientIpFilter();
        filter.setHeaderName("X-Forwarded-For");
        filter.setDirectIp(false);
        filter.setIpIndex(-2);

        FilterRegistrationBean<ClientIpFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        return registration;
    }
}
