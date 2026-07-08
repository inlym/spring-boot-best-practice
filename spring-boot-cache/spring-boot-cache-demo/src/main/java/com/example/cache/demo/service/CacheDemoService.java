package com.example.cache.demo.service;

import static com.example.cache.demo.config.CacheDemoTtlCustomizer.CACHE_NAME_USERS;

import com.example.cache.demo.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Spring Cache 注解演示服务
 *
 * <h2>说明
 * <p>使用线程安全的 List 模拟数据库存储，演示 Spring Cache 全部缓存注解的典型用法。
 * <p>所有缓存操作以 Redis 为后端存储，由 CacheConfig 统一配置。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
@Service
public class CacheDemoService {

    /** 全量用户列表缓存键 */
    private static final String ALL_USERS_KEY = "'all'";

    /** 模拟数据库存储 */
    private final List<User> database = new CopyOnWriteArrayList<>();

    /** 自增 ID 生成器 */
    private final AtomicLong idGenerator = new AtomicLong(0);

    // ================================ public 方法 ================================

    /**
     * 按 ID 查询用户
     *
     * <h3>缓存行为
     * <p>首次查询从模拟数据库加载并写入缓存，后续命中缓存直接返回。
     * <p>{@code unless} 条件：结果为 null 时不缓存，避免缓存穿透。
     *
     * @param userId 用户 ID
     * @return 用户信息，不存在时为 null
     */
    @Cacheable(cacheNames = CACHE_NAME_USERS, key = "#userId", unless = "#result == null")
    public User getByUserId(Long userId) {
        // 模拟数据库查询，延时 100ms
        simulateDatabaseLatency();

        // 从模拟数据库按 ID 查找
        User user = findById(userId);

        // 未命中时打印日志，演示缓存生效前后的差异
        log.info("数据库查询：userId={}, 命中={}", userId, user != null);

        return user;
    }

    /**
     * 查询全量用户列表
     *
     * <h3>缓存行为
     * <p>缓存全量列表，创建、更新、删除用户时同步驱逐此缓存。
     *
     * @return 用户列表，无数据时返回空列表
     */
    @Cacheable(cacheNames = CACHE_NAME_USERS, key = ALL_USERS_KEY)
    public List<User> listAllUsers() {
        // 模拟数据库查询，延时 100ms
        simulateDatabaseLatency();

        // 从模拟数据库读取全量数据
        List<User> users = new ArrayList<>(database);

        log.info("数据库全量查询：共 {} 条", users.size());

        return users;
    }

    /**
     * 创建用户
     *
     * <h3>缓存行为
     * <p>{@code @CachePut}：将新用户写入单条缓存。
     * <p>{@code @CacheEvict}：驱逐全量列表缓存，保证列表数据一致性。
     *
     * @param dto 用户信息（不含 ID）
     * @return 创建后的用户信息（含自动生成的 ID）
     */
    @Caching(
        put = {@CachePut(cacheNames = CACHE_NAME_USERS, key = "#result.id")},
        evict = {@CacheEvict(cacheNames = CACHE_NAME_USERS, key = ALL_USERS_KEY)}
    )
    public User createUser(User dto) {
        // 生成自增 ID
        Long id = idGenerator.incrementAndGet();

        // 构建完整用户对象并写入模拟数据库
        User user = User
            .builder()
            .id(id)
            .username(dto.getUsername())
            .email(dto.getEmail())
            .age(dto.getAge())
            .createTime(Instant.now())
            .build();
        database.add(user);

        log.info("创建用户：id={}, username={}", id, user.getUsername());

        return user;
    }

    /**
     * 更新用户
     *
     * <h3>缓存行为
     * <p>{@code @CachePut}：用最新数据刷新单条缓存。
     * <p>{@code @CacheEvict}：驱逐全量列表缓存，保证列表数据一致性。
     *
     * @param userId 用户 ID
     * @param dto    新的用户信息
     * @return 更新后的用户信息，用户不存在时为 null
     */
    @Caching(
        put = {@CachePut(cacheNames = CACHE_NAME_USERS, key = "#userId", unless = "#result == null")},
        evict = {@CacheEvict(cacheNames = CACHE_NAME_USERS, key = ALL_USERS_KEY)}
    )
    public User updateUser(Long userId, User dto) {
        // 检查用户是否存在
        User existing = findById(userId);
        if (existing == null) {
            log.trace("用户不存在，跳过更新，userId={}", userId);
            return null;
        }

        // 构建更新后的用户对象并替换模拟数据库中的旧记录
        User updated = User
            .builder()
            .id(userId)
            .username(dto.getUsername())
            .email(dto.getEmail())
            .age(dto.getAge())
            .createTime(existing.getCreateTime())
            .build();
        database.replaceAll(u -> userId.equals(u.getId()) ? updated : u);

        log.info("更新用户：userId={}, username={}", userId, updated.getUsername());

        return updated;
    }

    /**
     * 删除用户
     *
     * <h3>缓存行为
     * <p>{@code @CacheEvict}：驱逐单条缓存和全量列表缓存。
     * <p>{@code beforeInvocation = true}：在方法执行前驱逐，即使删除失败也不残留脏缓存。
     *
     * @param userId 用户 ID
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = CACHE_NAME_USERS, key = "#userId", beforeInvocation = true),
        @CacheEvict(cacheNames = CACHE_NAME_USERS, key = ALL_USERS_KEY, beforeInvocation = true)
    })
    public void deleteUser(Long userId) {
        // 从模拟数据库按 ID 移除
        database.removeIf(u -> userId.equals(u.getId()));

        log.info("删除用户：userId={}", userId);
    }

    /**
     * 清空全量列表缓存
     *
     * <h3>缓存行为
     * <p>{@code allEntries = true}：清除 users 缓存空间下所有条目。
     * <p>适用于缓存键无法提前枚举的批量失效场景。
     */
    @CacheEvict(cacheNames = CACHE_NAME_USERS, allEntries = true)
    public void clearAllCache() {
        // 方法体为空，缓存驱逐由注解完成
        log.info("已清空 users 缓存空间下所有条目");
    }

    /**
     * 按 ID 查询用户（条件缓存演示）
     *
     * <h3>缓存行为
     * <p>{@code condition}：仅当 userId 大于 0 时才使用缓存，防止无效 ID 污染缓存空间。
     * <p>{@code unless}：结果为 null 时不缓存，避免缓存穿透。
     *
     * @param userId 用户 ID
     * @return 用户信息，不存在时为 null
     */
    @Cacheable(cacheNames = CACHE_NAME_USERS, key = "#userId", condition = "#userId > 0", unless = "#result == null")
    public User getByUserIdWithCondition(Long userId) {
        // 模拟数据库查询，延时 100ms
        simulateDatabaseLatency();

        // 从模拟数据库按 ID 查找
        User user = findById(userId);

        log.info("条件缓存-数据库查询：userId={}, 命中={}", userId, user != null);

        return user;
    }

    // ================================ private 方法 ================================

    /**
     * 按 ID 查找用户
     *
     * @param userId 用户 ID
     * @return 用户信息，不存在时为 null
     */
    private User findById(Long userId) {
        return database
            .stream()
            .filter(u -> userId.equals(u.getId()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 模拟数据库查询延时
     *
     * <h3>说明
     * <p>使首次查询（缓存未命中）与后续查询（缓存命中）的响应时间差异明显，便于观察缓存效果。
     */
    private void simulateDatabaseLatency() {
        // Thread.sleep 抛出受检 InterruptedException，不捕获需在调用链逐层声明 throws
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.trace("模拟数据库延时被中断");
        }
    }
}
