package com.example.cache.demo.controller;

import com.example.cache.demo.model.EmptyResponse;
import com.example.cache.demo.model.User;
import com.example.cache.demo.service.CacheDemoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Spring Cache 缓存注解演示控制器
 *
 * <h2>功能说明
 * <p>提供演示 Spring Cache 全部缓存注解的 HTTP 端点，覆盖 @Cacheable、@CachePut、@CacheEvict、@Caching 及 condition/unless 条件表达式。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Validated
public class CacheDemoController {

    /** 缓存演示服务 */
    private final CacheDemoService cacheDemoService;

    // ================================ public 方法 ================================

    /**
     * 按 ID 查询用户
     *
     * <h3>缓存行为
     * <p>首次请求查询数据库（延时 100ms），响应后写入缓存。
     * <p>后续相同 ID 的请求直接命中缓存，响应时间显著缩短。
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    @GetMapping("/cache/users/{userId}")
    public User getByUserId(@PathVariable Long userId) {
        // 调用服务查询用户，缓存自动生效
        return cacheDemoService.getByUserId(userId);
    }

    /**
     * 查询全量用户列表
     *
     * <h3>缓存行为
     * <p>首次请求查询数据库并缓存全量列表，后续命中缓存。
     * <p>创建、更新、删除用户后此缓存自动失效，下次查询重新加载。
     *
     * @return 用户列表
     */
    @GetMapping("/cache/users")
    public List<User> listAllUsers() {
        // 调用服务查询全量用户，缓存自动生效
        return cacheDemoService.listAllUsers();
    }

    /**
     * 创建用户
     *
     * <h3>缓存行为
     * <p>将新建用户写入单条缓存（@CachePut），同时驱逐全量列表缓存（@CacheEvict）。
     *
     * @param dto 用户信息
     * @return 创建后的用户信息（含自动生成的 ID）
     */
    @PostMapping("/cache/users")
    public User createUser(@RequestBody @Valid UserSaveDTO dto) {
        // 构建用户信息并调用服务创建
        User user = User
            .builder()
            .username(dto.getUsername())
            .email(dto.getEmail())
            .age(dto.getAge())
            .build();

        return cacheDemoService.createUser(user);
    }

    /**
     * 更新用户
     *
     * <h3>缓存行为
     * <p>用最新数据刷新单条缓存（@CachePut），同时驱逐全量列表缓存（@CacheEvict）。
     *
     * @param userId 用户 ID
     * @param dto    新的用户信息
     * @return 更新后的用户信息
     */
    @PutMapping("/cache/users/{userId}")
    public User updateUser(@PathVariable Long userId, @RequestBody @Valid UserSaveDTO dto) {
        // 构建用户信息并调用服务更新
        User user = User
            .builder()
            .username(dto.getUsername())
            .email(dto.getEmail())
            .age(dto.getAge())
            .build();

        return cacheDemoService.updateUser(userId, user);
    }

    /**
     * 删除用户
     *
     * <h3>缓存行为
     * <p>驱逐单条缓存和全量列表缓存（@CacheEvict，beforeInvocation = true）。
     *
     * @param userId 用户 ID
     * @return 操作成功响应
     */
    @DeleteMapping("/cache/users/{userId}")
    public EmptyResponse deleteUser(@PathVariable Long userId) {
        // 调用服务删除用户，缓存自动驱逐
        cacheDemoService.deleteUser(userId);

        return EmptyResponse.success();
    }

    /**
     * 清空全量缓存
     *
     * <h3>缓存行为
     * <p>清除 users 缓存空间下所有条目（@CacheEvict，allEntries = true）。
     *
     * @return 操作成功响应
     */
    @DeleteMapping("/cache")
    public EmptyResponse clearAllCache() {
        // 调用服务清空缓存，注解自动执行
        cacheDemoService.clearAllCache();

        return EmptyResponse.success();
    }

    /**
     * 按 ID 查询用户（条件缓存演示）
     *
     * <h3>缓存行为
     * <p>仅当 userId > 0 时使用缓存（condition），结果为空时不写入缓存（unless）。
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    @GetMapping("/cache/users/{userId}/conditional")
    public User getByUserIdWithCondition(@PathVariable Long userId) {
        // 调用服务查询用户，条件缓存自动生效
        return cacheDemoService.getByUserIdWithCondition(userId);
    }

    // ================================ 请求模型 ================================

    /**
     * 用户保存请求模型
     *
     * @author <a href="https://www.inlym.com">inlym</a>
     * @since 1.0.0
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSaveDTO {

        /** 用户名 */
        @NotBlank
        private String username;

        /** 邮箱 */
        @NotBlank
        private String email;

        /** 年龄 */
        private Integer age;
    }
}
