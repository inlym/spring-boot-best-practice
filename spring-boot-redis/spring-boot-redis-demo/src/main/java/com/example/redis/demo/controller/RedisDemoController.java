package com.example.redis.demo.controller;

import com.example.redis.demo.model.UserInfoDTO;
import com.example.redis.demo.service.RedisDemoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Redis 数据处理演示控制器
 *
 * <h2>功能说明
 * <p>提供演示三类 RedisTemplate 存取用法的 HTTP 端点：泛化对象模板（@class 保留类型）、具化对象模板（无类型信息）、二进制模板（原生字节数组）。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Validated
public class RedisDemoController {

    /** Redis 数据处理演示服务 */
    private final RedisDemoService redisDemoService;

    // ================================ public 方法 ================================

    /**
     * 保存对象（泛化模板）
     *
     * <h3>处理逻辑
     * <p>经泛化模板写入用户信息，序列化结果含 @class 字段，并回显已存储的对象。
     *
     * @param key Redis 键
     * @param dto 用户信息
     * @return 已存储的用户信息
     */
    @PostMapping("/redis/objects/{key}")
    public UserInfoDTO saveObject(@PathVariable String key, @RequestBody @Valid UserInfoDTO dto) {
        // 经泛化模板写入 Redis
        redisDemoService.saveObject(key, dto);
        // 回显已存储的用户信息
        return dto;
    }

    /**
     * 查询对象（泛化模板）
     *
     * <h3>处理逻辑
     * <p>从泛化模板读取用户信息，@class 保证反序列化还原为原始类型。
     *
     * @param key Redis 键
     * @return 用户信息，键不存在时为 null
     */
    @GetMapping("/redis/objects/{key}")
    public UserInfoDTO findObject(@PathVariable String key) {
        // 从泛化模板读取并返回
        return redisDemoService.findObject(key);
    }

    /**
     * 保存用户（具化模板）
     *
     * <h3>处理逻辑
     * <p>经 UserInfoDTO 专用模板写入用户信息，序列化结果不含 @class，并回显已存储的对象。
     *
     * @param key Redis 键
     * @param dto 用户信息
     * @return 已存储的用户信息
     */
    @PostMapping("/redis/users/{key}")
    public UserInfoDTO saveTypedUser(@PathVariable String key, @RequestBody @Valid UserInfoDTO dto) {
        // 经具化模板写入 Redis
        redisDemoService.saveTypedUser(key, dto);
        // 回显已存储的用户信息
        return dto;
    }

    /**
     * 查询用户（具化模板）
     *
     * <h3>处理逻辑
     * <p>从具化模板读取用户信息，直接返回 UserInfoDTO 类型，无需转型。
     *
     * @param key Redis 键
     * @return 用户信息，键不存在时为 null
     */
    @GetMapping("/redis/users/{key}")
    public UserInfoDTO findTypedUser(@PathVariable String key) {
        // 从具化模板读取并返回
        return redisDemoService.findTypedUser(key);
    }

    /**
     * 保存二进制
     *
     * <h3>处理逻辑
     * <p>经二进制模板写入原始字节，不经 JSON 编解码，并回显已存储的字节数组。
     *
     * @param key Redis 键
     * @param dto 二进制内容
     * @return 已存储的二进制内容
     */
    @PostMapping("/redis/binaries/{key}")
    public BinaryVO saveBinary(@PathVariable String key, @RequestBody @Valid BinarySaveDTO dto) {
        // 经二进制模板写入原始字节
        redisDemoService.saveBinary(key, dto.getData());
        // 回显已存储的字节数组
        return BinaryVO.builder().data(dto.getData()).build();
    }

    /**
     * 查询二进制
     *
     * <h3>处理逻辑
     * <p>从二进制模板读取原始字节，直接返回字节数组。
     *
     * @param key Redis 键
     * @return 二进制内容，键不存在时为 null
     */
    @GetMapping("/redis/binaries/{key}")
    public BinaryVO findBinary(@PathVariable String key) {
        // 从二进制模板读取原始字节并返回
        byte[] data = redisDemoService.findBinary(key);

        return BinaryVO.builder().data(data).build();
    }

    /**
     * 保存列表（泛化模板）
     *
     * <h3>处理逻辑
     * <p>经泛化模板写入字符串列表，Jackson 序列化为 JSON 数组并附带 @class 类型信息。
     *
     * @param key Redis 键
     * @param dto 字符串列表
     * @return 已存储的字符串列表
     */
    @PostMapping("/redis/lists/{key}")
    public List<String> saveList(@PathVariable String key, @RequestBody @Valid StringListDTO dto) {
        // 经泛化模板写入 Redis
        redisDemoService.saveList(key, dto.getItems());
        // 回显已存储的列表
        return dto.getItems();
    }

    /**
     * 查询列表（泛化模板）
     *
     * <h3>处理逻辑
     * <p>从泛化模板读取字符串列表，@class 保证反序列化还原为 List 类型。
     *
     * @param key Redis 键
     * @return 字符串列表，键不存在时为 null
     */
    @GetMapping("/redis/lists/{key}")
    public List<String> findList(@PathVariable String key) {
        // 从泛化模板读取并返回
        return redisDemoService.findList(key);
    }

    /**
     * 保存 Long（泛化模板）
     *
     * <h3>处理逻辑
     * <p>经泛化模板写入 Long 值，Jackson 序列化为 JSON 数字并附带 @class 类型信息。
     *
     * @param key Redis 键
     * @param dto Long 值
     * @return 已存储的 Long 值
     */
    @PostMapping("/redis/longs/{key}")
    public Long saveLong(@PathVariable String key, @RequestBody @Valid LongValueDTO dto) {
        // 经泛化模板写入 Redis
        redisDemoService.saveLong(key, dto.getValue());
        // 回显已存储的 Long 值
        return dto.getValue();
    }

    /**
     * 查询 Long（泛化模板）
     *
     * <h3>处理逻辑
     * <p>从泛化模板读取 Long 值，@class 保证反序列化还原为 Long 类型。
     *
     * @param key Redis 键
     * @return Long 值，键不存在时为 null
     */
    @GetMapping("/redis/longs/{key}")
    public Long findLong(@PathVariable String key) {
        // 从泛化模板读取并返回
        return redisDemoService.findLong(key);
    }

    /**
     * 保存 Integer（泛化模板）
     *
     * <h3>处理逻辑
     * <p>经泛化模板写入 Integer 值，Jackson 序列化为 JSON 数字并附带 @class 类型信息。
     *
     * @param key Redis 键
     * @param dto Integer 值
     * @return 已存储的 Integer 值
     */
    @PostMapping("/redis/integers/{key}")
    public Integer saveInteger(@PathVariable String key, @RequestBody @Valid IntegerValueDTO dto) {
        // 经泛化模板写入 Redis
        redisDemoService.saveInteger(key, dto.getValue());
        // 回显已存储的 Integer 值
        return dto.getValue();
    }

    /**
     * 查询 Integer（泛化模板）
     *
     * <h3>处理逻辑
     * <p>从泛化模板读取 Integer 值，@class 保证反序列化还原为 Integer 类型。
     *
     * @param key Redis 键
     * @return Integer 值，键不存在时为 null
     */
    @GetMapping("/redis/integers/{key}")
    public Integer findInteger(@PathVariable String key) {
        // 从泛化模板读取并返回
        return redisDemoService.findInteger(key);
    }

    /**
     * 保存 Boolean（泛化模板）
     *
     * <h3>处理逻辑
     * <p>经泛化模板写入 Boolean 值，Jackson 序列化为 JSON 布尔值并附带 @class 类型信息。
     *
     * @param key Redis 键
     * @param dto Boolean 值
     * @return 已存储的 Boolean 值
     */
    @PostMapping("/redis/booleans/{key}")
    public Boolean saveBoolean(@PathVariable String key, @RequestBody @Valid BooleanValueDTO dto) {
        // 经泛化模板写入 Redis
        redisDemoService.saveBoolean(key, dto.getValue());
        // 回显已存储的 Boolean 值
        return dto.getValue();
    }

    /**
     * 查询 Boolean（泛化模板）
     *
     * <h3>处理逻辑
     * <p>从泛化模板读取 Boolean 值，@class 保证反序列化还原为 Boolean 类型。
     *
     * @param key Redis 键
     * @return Boolean 值，键不存在时为 null
     */
    @GetMapping("/redis/booleans/{key}")
    public Boolean findBoolean(@PathVariable String key) {
        // 从泛化模板读取并返回
        return redisDemoService.findBoolean(key);
    }

    // ================================ 请求模型 ================================

    /**
     * 字符串列表保存请求模型
     *
     * @author <a href="https://www.inlym.com">inlym</a>
     * @since 1.0.0
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StringListDTO {

        /**
         * 字符串列表
         *
         * <h3>字段说明
         * <p>Jackson 序列化为 JSON 数组，元素均为字符串类型。
         */
        @NotNull
        private List<String> items;
    }

    /**
     * Long 值保存请求模型
     *
     * @author <a href="https://www.inlym.com">inlym</a>
     * @since 1.0.0
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LongValueDTO {

        /** Long 值 */
        @NotNull
        private Long value;
    }

    /**
     * Integer 值保存请求模型
     *
     * @author <a href="https://www.inlym.com">inlym</a>
     * @since 1.0.0
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntegerValueDTO {

        /** Integer 值 */
        @NotNull
        private Integer value;
    }

    /**
     * Boolean 值保存请求模型
     *
     * @author <a href="https://www.inlym.com">inlym</a>
     * @since 1.0.0
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BooleanValueDTO {

        /** Boolean 值 */
        @NotNull
        private Boolean value;
    }

    /**
     * 二进制保存请求模型
     *
     * @author <a href="https://www.inlym.com">inlym</a>
     * @since 1.0.0
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BinarySaveDTO {

        /**
         * 二进制内容
         *
         * <h3>字段说明
         * <p>JSON 传输时由 Jackson 编解码为 Base64 字符串。
         */
        @NotNull
        private byte[] data;
    }

    // ================================ 响应模型 ================================

    /**
     * 二进制操作响应模型
     *
     * @author <a href="https://www.inlym.com">inlym</a>
     * @since 1.0.0
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BinaryVO {

        /**
         * 二进制内容
         *
         * <h3>字段说明
         * <p>JSON 传输时由 Jackson 编解码为 Base64 字符串。
         */
        private byte[] data;
    }
}
