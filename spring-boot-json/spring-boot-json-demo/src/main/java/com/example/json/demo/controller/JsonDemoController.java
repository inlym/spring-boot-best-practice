package com.example.json.demo.controller;

import com.example.json.demo.model.UserInfoDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * JSON 处理演示控制器
 *
 * <h2>功能说明
 * <p>提供演示控制器层面 JSON 序列化与反序列化效果的 HTTP 端点，覆盖请求体自动反序列化（@RequestBody）、响应体自动序列化、Instant 毫秒时间戳和 null 字段过滤，不涉及非 HTTP 场景下的手动 JSON 处理。
 *
 * @module JSON 处理
 * @folder JSON 处理
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@RestController
public class JsonDemoController {

    /**
     * 回显用户信息
     * 接收请求体并原样返回，演示 @RequestBody 自动反序列化、响应体自动序列化，以及 Instant 毫秒时间戳和 null 字段过滤效果。
     *
     * @param dto 用户信息请求数据
     * @return 用户信息响应数据
     */
    @PostMapping("/users")
    public UserInfoDTO echo(@RequestBody UserInfoDTO dto) {
        return dto;
    }

    /**
     * 获取示例用户
     * 返回构造的示例对象，演示响应体自动序列化效果：Instant 序列化为毫秒时间戳、空字段被 NON_NULL 过滤。
     *
     * @return 示例用户信息
     */
    @GetMapping("/users/sample")
    public UserInfoDTO sample() {
        // 构造示例用户对象，remark 留空以演示 NON_NULL 过滤效果
        return UserInfoDTO.builder().username("alice").age(28).createTime(Instant.now()).build();
    }
}
