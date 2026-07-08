package com.example.mybatisflex.demo.service;

import com.example.mybatisflex.demo.entity.User;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 *
 * <h2>说明
 * <p>继承 MyBatis-Flex 的 BaseMapper 接口，自动获得 CRUD 基础能力，无需手动编写 SQL。
 * <p>MyBatis-Flex 配置已开启自动生成 Mapper 实现，无需手动创建 XML 映射文件。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
