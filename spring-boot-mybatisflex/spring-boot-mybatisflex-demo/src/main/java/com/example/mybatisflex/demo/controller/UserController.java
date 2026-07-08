package com.example.mybatisflex.demo.controller;

import com.example.mybatisflex.demo.entity.User;
import com.example.mybatisflex.demo.model.EmptyResponse;
import com.example.mybatisflex.demo.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制器
 *
 * <h2>功能说明
 * <p>提供用户增删改查的 REST 端点，演示 MyBatis-Flex ORM 框架在 HTTP 层的完整集成用法。
 * <p>覆盖 save 新增、主键查询、用户名查询、Builder 方式更新和逻辑删除五种典型操作。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@RestController
@Validated
@RequiredArgsConstructor
public class UserController {

    /** 用户服务 */
    private final UserService userService;

    // ================================ public 方法 ================================

    /**
     * 创建用户
     *
     * @param dto 用户创建请求数据
     * @return 创建后的用户信息，包含自动生成的主键 ID
     */
    @PostMapping("/users")
    public User create(@RequestBody @Valid User dto) {
        // save 方法内部调用 insertSelective，仅插入非 null 字段，数据库默认值生效
        userService.save(dto);
        return dto;
    }

    /**
     * 按主键查询用户
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    @GetMapping("/users/{userId}")
    public User getById(@PathVariable Long userId) {
        User user = userService.getById(userId);

        // IService.getById 未找到时返回 null，控制器层转为异常以符合 getBy* 命名契约
        if (user == null) {
            throw new IllegalArgumentException(String.format("用户不存在，userId=%d", userId));
        }
        return user;
    }

    /**
     * 按用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息，不存在时返回 null
     */
    @GetMapping("/users/by-username")
    public User getByUsername(@RequestParam @NotBlank String username) {
        // findByUsername 未找到时返回 null，符合 findBy* 命名契约
        return userService.findByUsername(username);
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户 ID
     * @param dto    用户更新请求数据
     * @return 更新后的用户信息
     */
    @PutMapping("/users/{userId}")
    public User update(
        @PathVariable Long userId,
        @RequestBody User dto
    ) {
        // 使用 Builder 方式构建更新实例，仅含主键和需更新的字段
        User updateUser = User.builder()
            .id(userId)
            .email(dto.getEmail())
            .age(dto.getAge())
            .build();

        // updateById 按主键更新，忽略 null 字段
        userService.updateById(updateUser);

        // 更新后重新查询以获取最新数据
        User updated = userService.getById(userId);
        if (updated == null) {
            throw new IllegalArgumentException(String.format("用户不存在，userId=%d", userId));
        }
        return updated;
    }

    /**
     * 删除用户
     *
     * <h3>删除方式
     * <p>使用逻辑删除，delete_time 字段被填充为当前时间戳，数据不会从数据库中物理移除。
     *
     * @param userId 用户 ID
     * @return 空响应
     */
    @DeleteMapping("/users/{userId}")
    public EmptyResponse delete(@PathVariable Long userId) {
        // removeById 触发逻辑删除，delete_time 字段自动填充当前时间戳
        userService.removeById(userId);
        return EmptyResponse.success();
    }
}
