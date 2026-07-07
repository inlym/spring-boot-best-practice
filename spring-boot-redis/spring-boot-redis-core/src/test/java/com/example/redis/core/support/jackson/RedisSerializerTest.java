package com.example.redis.core.support.jackson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis Jackson 3 序列化器测试
 *
 * <h2>说明
 * <p>验证 GenericJackson3JsonRedisSerializer（泛化模板，含 @class）与 Jackson3JsonRedisSerializer（具化模板，无类型信息）的序列化与反序列化行为。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
class RedisSerializerTest {

    /** 被测对象所用的 JSON 映射器，保持默认配置即可验证序列化器自身行为 */
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    // ================================ 泛化模板（含 @class） ================================

    /**
     * 泛化序列化器输出含 @class 字段
     *
     * <h3>验证行为
     * <p>GenericJackson3JsonRedisSerializer 在序列化结果中写入 @class 记录原始类型信息。
     */
    @Test
    void genericSerializerWritesClassInfo() {
        GenericJackson3JsonRedisSerializer serializer = new GenericJackson3JsonRedisSerializer(jsonMapper);

        byte[] bytes = serializer.serialize(new TestUser("alice", 28));

        String json = new String(bytes);
        assertThat(json).contains("@class");
    }

    /**
     * 泛化序列化器 null 入参加密返回空字节数组
     *
     * <h3>验证行为
     * <p>与 Spring Data Redis 标准实现一致，null 输入序列化为空字节数组而非抛出异常。
     */
    @Test
    void genericSerializerNullReturnsEmptyArray() {
        GenericJackson3JsonRedisSerializer serializer = new GenericJackson3JsonRedisSerializer(jsonMapper);

        byte[] bytes = serializer.serialize(null);

        assertThat(bytes).isEmpty();
    }

    /**
     * 泛化反序列化器通过 @class 还原为原始类型
     *
     * <h3>验证行为
     * <p>序列化结果中含 @class 字段，反序列化时自动还原为具体类型。
     */
    @Test
    void genericDeserializerRestoresTypeViaClassInfo() {
        GenericJackson3JsonRedisSerializer serializer = new GenericJackson3JsonRedisSerializer(jsonMapper);

        byte[] bytes = serializer.serialize(new TestUser("bob", 32));
        Object result = serializer.deserialize(bytes);

        // 反序列化结果实际类型应与序列化前的类型一致
        assertThat(result).isInstanceOf(TestUser.class);
    }

    /**
     * 泛化反序列化器 null 或空字节数组入参返回 null
     */
    @Test
    void genericDeserializerNullAndEmptyReturnsNull() {
        GenericJackson3JsonRedisSerializer serializer = new GenericJackson3JsonRedisSerializer(jsonMapper);

        assertThat(serializer.deserialize(null)).isNull();
        assertThat(serializer.deserialize(new byte[0])).isNull();
    }

    // ================================ 具化模板（无 @class） ================================

    /**
     * 具化序列化器输出不含 @class 字段
     *
     * <h3>验证行为
     * <p>Jackson3JsonRedisSerializer 按构造时绑定的类型序列化，不附加类型信息，存储体积更小。
     */
    @Test
    void typedSerializerOmitsClassInfo() {
        Jackson3JsonRedisSerializer<TestUser> serializer = new Jackson3JsonRedisSerializer<>(jsonMapper, TestUser.class);

        byte[] bytes = serializer.serialize(new TestUser("alice", 28));

        String json = new String(bytes);
        assertThat(json).doesNotContain("@class");
    }

    /**
     * 具化序列化器 null 入参加密返回空字节数组
     *
     * <h3>验证行为
     * <p>与 Spring Data Redis 标准实现一致，null 输入序列化为空字节数组。
     */
    @Test
    void typedSerializerNullReturnsEmptyArray() {
        Jackson3JsonRedisSerializer<TestUser> serializer = new Jackson3JsonRedisSerializer<>(jsonMapper, TestUser.class);

        byte[] bytes = serializer.serialize(null);

        assertThat(bytes).isEmpty();
    }

    /**
     * 具化反序列化器按绑定类型还原对象
     *
     * <h3>验证行为
     * <p>Jackson3JsonRedisSerializer 按构造时绑定的类型反序列化，直接返回目标类型，无需手动转型。
     */
    @Test
    void typedDeserializerRestoresToBoundType() {
        Jackson3JsonRedisSerializer<TestUser> serializer = new Jackson3JsonRedisSerializer<>(jsonMapper, TestUser.class);
        TestUser original = new TestUser("carol", 25);

        byte[] bytes = serializer.serialize(original);
        TestUser result = serializer.deserialize(bytes);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("carol");
        assertThat(result.getAge()).isEqualTo(25);
    }

    /**
     * 具化反序列化器 null 或空字节数组入参返回 null
     */
    @Test
    void typedDeserializerNullAndEmptyReturnsNull() {
        Jackson3JsonRedisSerializer<TestUser> serializer = new Jackson3JsonRedisSerializer<>(jsonMapper, TestUser.class);

        assertThat(serializer.deserialize(null)).isNull();
        assertThat(serializer.deserialize(new byte[0])).isNull();
    }

    // ================================ 测试专用类型 ================================

    /** 测试用简单对象，包含基础字段类型以验证序列化往返 */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestUser {

        /** 用户名 */
        private String username;

        /** 年龄 */
        private Integer age;
    }
}
