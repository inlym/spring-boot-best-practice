package com.example.cache.demo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 空响应模型
 *
 * <h2>说明
 * <p>用于无返回内容的 API 端点，返回统一的成功标识，替代 void 返回值。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmptyResponse {

    /** 操作是否成功 */
    private final Boolean success;

    /** 成功响应单例 */
    private static final EmptyResponse SUCCESS = new EmptyResponse(true);

    /**
     * 获取成功响应实例
     *
     * @return 不可变的成功响应单例
     */
    public static EmptyResponse success() {
        return SUCCESS;
    }
}
