package com.example.mybatisflex.demo.service;

import com.example.mybatisflex.demo.entity.User;
import com.example.mybatisflex.demo.mapper.UserMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 *
 * <h2>说明
 * <p>直接使用 UserMapper 进行 ORM 操作。
 * <p>演示自定义查询方法 findByUsername，展示 QueryWrapper 条件查询的典型用法。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserService {

    /** 用户 Mapper，由 MyBatis-Flex 自动生成实现并注入 */
    private final UserMapper userMapper;

    // ================================ public 方法 ================================

    /**
     * 创建用户
     *
     * <h3>插入策略
     * <p>使用 insertSelective，仅插入非 null 字段，数据库默认值对未提供字段生效。
     *
     * @param user 用户实体
     */
    public void save(User user) {
        userMapper.insertSelective(user);
    }

    /**
     * 按主键查询用户
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    public User getById(Long userId) {
        User user = userMapper.selectOneById(userId);

        // selectOneById 未找到时返回 null，转为异常以符合 getBy* 命名契约
        if (user == null) {
            throw new IllegalArgumentException(String.format("用户不存在，userId=%d", userId));
        }
        return user;
    }

    /**
     * 按用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息，不存在时为 null
     */
    public User findByUsername(String username) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select()
            .where(User::getUsername).eq(username);

        // selectOneByQuery 在无结果时返回 null，符合 findBy* 命名契约
        return userMapper.selectOneByQuery(queryWrapper);
    }

    /**
     * 更新用户
     *
     * <h3>更新策略
     * <p>按主键更新，忽略 null 字段。
     *
     * @param user 用户实体，仅含主键和需更新的字段
     */
    public void update(User user) {
        userMapper.update(user);
    }

    /**
     * 删除用户
     *
     * <h3>删除方式
     * <p>触发逻辑删除，delete_time 字段自动填充当前时间戳。
     *
     * @param userId 用户 ID
     */
    public void deleteById(Long userId) {
        userMapper.deleteById(userId);
    }
}
