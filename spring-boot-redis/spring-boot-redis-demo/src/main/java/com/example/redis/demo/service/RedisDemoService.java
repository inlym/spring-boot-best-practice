package com.example.redis.demo.service;

import com.example.redis.core.service.RedisTemplateService;
import com.example.redis.demo.model.UserInfoDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Redis 数据处理演示服务
 *
 * <h2>说明
 * <p>演示三类 RedisTemplate 的存取用法：泛化对象模板（@class 保留类型）、具化对象模板（无类型信息）、二进制模板（原生字节数组）。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class RedisDemoService {

    /** 泛化对象模板，值以 JSON 序列化并通过 @class 携带类型信息 */
    private final RedisTemplate<String, Object> redisTemplate;

    /** 二进制模板，值以字节数组原生存储，适用于图片、音频等二进制内容 */
    private final RedisTemplate<String, byte[]> redisTemplateBytes;

    /** 类型安全模板创建工厂 */
    private final RedisTemplateService redisTemplateService;

    /** UserInfoDTO 专用模板，存储不含类型信息，反序列化直接得到目标类型 */
    private RedisTemplate<String, UserInfoDTO> userInfoTemplate;

    // ================================ public 方法 ================================

    /**
     * 保存对象（泛化模板）
     *
     * <h3>处理逻辑
     * <p>经泛化模板写入，序列化结果含 @class 字段记录原始类型，反序列化时据此还原。
     *
     * @param key  Redis 键
     * @param user 待存储的用户信息
     */
    public void saveObject(String key, UserInfoDTO user) {
        // 写入泛化模板，值序列化时附带 @class 类型信息
        redisTemplate.opsForValue().set(key, user);
    }

    /**
     * 查询对象（泛化模板）
     *
     * <h3>处理逻辑
     * <p>泛化模板读取后返回 Object，因 @class 已记录原始类型，实际为 UserInfoDTO，可直接转型。
     *
     * @param key Redis 键
     * @return 用户信息，键不存在时为 null
     */
    public UserInfoDTO findObject(String key) {
        // 读取泛化模板，@class 保证反序列化得到原始 UserInfoDTO 类型
        Object value = redisTemplate.opsForValue().get(key);

        // 泛化模板依赖 @class 还原类型，运行时存在类型不匹配的极端可能（如缓存被外部修改），做防御性检查
        if (
            value != null
            && !(value instanceof UserInfoDTO)
        ) {
            throw new IllegalStateException(
                String.format("Redis 缓存类型不匹配，期望 UserInfoDTO，实际 %s", value.getClass().getName())
            );
        }

        return (UserInfoDTO) value;
    }

    /**
     * 保存用户（具化模板）
     *
     * <h3>处理逻辑
     * <p>经 UserInfoDTO 专用模板写入，序列化结果不含 @class，存储体积更小。
     *
     * @param key  Redis 键
     * @param user 待存储的用户信息
     */
    public void saveTypedUser(String key, UserInfoDTO user) {
        // 写入具化模板，按 UserInfoDTO 类型序列化，不附加类型信息
        userInfoTemplate.opsForValue().set(key, user);
    }

    /**
     * 查询用户（具化模板）
     *
     * <h3>处理逻辑
     * <p>具化模板按构造时绑定的类型反序列化，直接返回 UserInfoDTO，无需转型。
     *
     * @param key Redis 键
     * @return 用户信息，键不存在时为 null
     */
    public UserInfoDTO findTypedUser(String key) {
        // 读取具化模板，直接返回 UserInfoDTO 类型
        return userInfoTemplate.opsForValue().get(key);
    }

    /**
     * 保存二进制
     *
     * <h3>处理逻辑
     * <p>经二进制模板写入，字节原样存储，不经 JSON 编解码，避免二进制内容损坏。
     *
     * @param key  Redis 键
     * @param data 待存储的字节数组
     */
    public void saveBinary(String key, byte[] data) {
        // 写入二进制模板，字节数组原生存储
        redisTemplateBytes.opsForValue().set(key, data);
    }

    /**
     * 查询二进制
     *
     * <h3>处理逻辑
     * <p>二进制模板读取原始字节，直接返回字节数组。
     *
     * @param key Redis 键
     * @return 字节数组，键不存在时为 null
     */
    public byte[] findBinary(String key) {
        // 读取二进制模板，返回原始字节数组
        return redisTemplateBytes.opsForValue().get(key);
    }

    /**
     * 保存列表（泛化模板）
     *
     * <h3>处理逻辑
     * <p>经泛化模板写入，List 序列化为 JSON 数组并附带 @class 类型信息。
     *
     * @param key   Redis 键
     * @param value 待存储的字符串列表
     */
    public void saveList(String key, List<String> value) {
        // 写入泛化模板，Jackson 将 List 序列化为 JSON 数组
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 查询列表（泛化模板）
     *
     * <h3>处理逻辑
     * <p>泛化模板读取后通过 @class 还原为 List，防御性检查确保类型安全。
     *
     * @param key Redis 键
     * @return 字符串列表，键不存在时为 null
     */
    public List<String> findList(String key) {
        // 读取泛化模板，@class 保证反序列化还原为 List 类型
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        // 泛化模板依赖 @class 还原类型，运行时做防御性类型检查
        if (!(value instanceof List<?> rawList)) {
            throw new IllegalStateException(
                String.format("Redis 缓存类型不匹配，期望 List，实际 %s", value.getClass().getName())
            );
        }

        // 逐元素转换为 String，避免泛型擦除导致的不安全类型转换
        return rawList.stream().map(item -> item != null ? item.toString() : null).toList();
    }

    /**
     * 保存 Long（泛化模板）
     *
     * <h3>处理逻辑
     * <p>经泛化模板写入，Long 序列化为 JSON 数字并附带 @class 类型信息。
     *
     * @param key   Redis 键
     * @param value 待存储的 Long 值
     */
    public void saveLong(String key, Long value) {
        // 写入泛化模板，Jackson 将 Long 序列化为 JSON 数字
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 查询 Long（泛化模板）
     *
     * <h3>处理逻辑
     * <p>泛化模板读取后通过 @class 还原为 Long，防御性检查确保类型安全。
     *
     * @param key Redis 键
     * @return Long 值，键不存在时为 null
     */
    public Long findLong(String key) {
        // 读取泛化模板，@class 保证反序列化还原为 Long 类型
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        // 泛化模板依赖 @class 还原类型，运行时做防御性类型检查
        if (!(value instanceof Long)) {
            throw new IllegalStateException(
                String.format("Redis 缓存类型不匹配，期望 Long，实际 %s", value.getClass().getName())
            );
        }

        return (Long) value;
    }

    /**
     * 保存 Integer（泛化模板）
     *
     * <h3>处理逻辑
     * <p>经泛化模板写入，Integer 序列化为 JSON 数字并附带 @class 类型信息。
     *
     * @param key   Redis 键
     * @param value 待存储的 Integer 值
     */
    public void saveInteger(String key, Integer value) {
        // 写入泛化模板，Jackson 将 Integer 序列化为 JSON 数字
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 查询 Integer（泛化模板）
     *
     * <h3>处理逻辑
     * <p>泛化模板读取后通过 @class 还原为 Integer，防御性检查确保类型安全。
     *
     * @param key Redis 键
     * @return Integer 值，键不存在时为 null
     */
    public Integer findInteger(String key) {
        // 读取泛化模板，@class 保证反序列化还原为 Integer 类型
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        // 泛化模板依赖 @class 还原类型，运行时做防御性类型检查
        if (!(value instanceof Integer)) {
            throw new IllegalStateException(
                String.format("Redis 缓存类型不匹配，期望 Integer，实际 %s", value.getClass().getName())
            );
        }

        return (Integer) value;
    }

    /**
     * 保存 Boolean（泛化模板）
     *
     * <h3>处理逻辑
     * <p>经泛化模板写入，Boolean 序列化为 JSON 布尔值并附带 @class 类型信息。
     *
     * @param key   Redis 键
     * @param value 待存储的 Boolean 值
     */
    public void saveBoolean(String key, Boolean value) {
        // 写入泛化模板，Jackson 将 Boolean 序列化为 JSON 布尔值
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 查询 Boolean（泛化模板）
     *
     * <h3>处理逻辑
     * <p>泛化模板读取后通过 @class 还原为 Boolean，防御性检查确保类型安全。
     *
     * @param key Redis 键
     * @return Boolean 值，键不存在时为 null
     */
    public Boolean findBoolean(String key) {
        // 读取泛化模板，@class 保证反序列化还原为 Boolean 类型
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        // 泛化模板依赖 @class 还原类型，运行时做防御性类型检查
        if (!(value instanceof Boolean)) {
            throw new IllegalStateException(
                String.format("Redis 缓存类型不匹配，期望 Boolean，实际 %s", value.getClass().getName())
            );
        }

        return (Boolean) value;
    }

    // ================================ private 方法 ================================

    /**
     * 初始化具化模板
     *
     * <h3>处理逻辑
     * <p>启动时通过工厂创建一次 UserInfoDTO 专用模板并缓存，避免每次操作重复构建序列化器。
     */
    @PostConstruct
    private void initUserInfoTemplate() {
        // 经工厂创建具化模板，整个生命周期复用同一实例
        userInfoTemplate = redisTemplateService.createRedisTemplate(UserInfoDTO.class);
    }
}
