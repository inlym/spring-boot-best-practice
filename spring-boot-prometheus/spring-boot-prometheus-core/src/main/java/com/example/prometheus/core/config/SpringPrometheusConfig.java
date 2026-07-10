package com.example.prometheus.core.config;

import java.util.List;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Prometheus 扩展配置类
 *
 * <h2>配置说明
 * <p>注册 Prometheus 指标相关的 MeterFilter Bean，为所有 Micrometer 指标添加通用标签，便于在多服务场景中按维度聚合与筛选。
 *
 * <h3>通用标签
 * <p>通过 MeterFilter 添加服务级别的通用标签，与 application.yml 中 management.metrics.tags 配置互补。
 * <p>此处以编程方式演示，实际项目中可根据需要选择配置方式。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Configuration
public class SpringPrometheusConfig {

    // ================================ public 方法 ================================

    /**
     * 通用标签 MeterFilter
     *
     * <h3>过滤逻辑
     * <p>为所有指标添加 framework 通用标签，标识指标来源为 Spring Boot 应用。
     * <p>Prometheus 建议使用有意义的标签来区分不同维度的指标，便于在 Grafana 等可视化工具中筛选。
     *
     * @return MeterFilter 实例
     */
    @Bean
    public MeterFilter commonTagsMeterFilter() {
        return MeterFilter.commonTags(List.of(Tag.of("framework", "spring-boot")));
    }
}
