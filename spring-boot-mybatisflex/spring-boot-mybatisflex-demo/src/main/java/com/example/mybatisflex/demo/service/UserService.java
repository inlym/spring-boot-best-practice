package com.example.mybatisflex.demo.service;

import com.example.mybatisflex.demo.entity.User;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 *
 * <h2>说明
 * <p>实现 MyBatis-Flex 的 IService 接口，自动获得 insertSelective、select、update、delete 等基础 ORM 操作能力。
 * <p>演示自定义查询方法 findByUsername，展示 QueryWrapper 条件查询的典型用法。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserService implements IService<User> {

    /** 用户 Mapper，由 MyBatis-Flex 自动生成实现并注入 */
    private final UserMapper userMapper;

    // ================================ public 方法 ================================

    /**
     * 获取基础 Mapper
     *
     * @return User 实体对应的 BaseMapper 实例
     */
    @Override
    @NonNull
    public BaseMapper<User> getMapper() {
        return userMapper;
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

        // getOne 在无结果时返回 null，符合 findBy* 命名契约
        return getOne(queryWrapper);
    }
}
