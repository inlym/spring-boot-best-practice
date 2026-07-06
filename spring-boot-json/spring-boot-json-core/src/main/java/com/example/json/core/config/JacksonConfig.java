package com.example.json.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;

import java.time.ZoneOffset;
import java.util.TimeZone;

/**
 * Jackson JSON 序列化配置类
 *
 * <h2>配置说明
 * <p>通过 JsonMapperBuilderCustomizer 向 Spring Boot 自动配置的 JsonMapper 注入自定义序列化策略，
 * <p>包括日期时间时区、序列化策略和反序列化容错策略。
 * <p>Instant 等时间类型统一序列化为毫秒时间戳，由 DateTimeFeature 三个开关控制，替代手写 Serializer/Deserializer。
 *
 * <h2>复用方式
 * <p>将本类复制到目标项目后，仅需修改包声明（package 语句）即可生效，无需任何额外配置。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 **/
@Configuration
public class JacksonConfig {

    /**
     * 定制 Jackson JsonMapper 构建器
     *
     * <h3>定制项
     * <p>反序列化忽略未知字段、未知枚举值映射为 null、空 Bean 序列化不抛异常、时区 UTC、Instant 序列化为毫秒时间戳、忽略 null 字段。
     *
     * @return JsonMapper 构建器定制器，不为 null
     */
    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
            // API 字段变更时不中断旧版本客户端的反序列化
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            // 第三方服务返回新枚举值时，映射为 null 而不中断反序列化
            .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            // DTO 字段全为 null 被 NON_NULL 过滤后，序列化空对象不抛异常
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            // 时间戳基准时区，避免因运行环境时区差异导致序列化结果不一致
            .defaultTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC))
            // 时间类型序列化为数字时间戳（覆盖默认的 ISO-8601 字符串格式），统一前后端数据交换格式
            .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            // 时间戳使用毫秒精度（默认纳秒），匹配前端 Number 安全整数范围
            .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            // 反序列化时将数字时间戳按毫秒解析（默认纳秒），与序列化端保持一致
            .disable(DateTimeFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            // 响应体不输出 null 字段，减少无效传输
            .changeDefaultPropertyInclusion(value -> value.withValueInclusion(JsonInclude.Include.NON_NULL));
    }
}
