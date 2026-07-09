package com.example.exception.core.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错误响应数据
 *
 * <h2>类说明
 * <p>用于封装请求处理过程中发生异常时的统一响应信息，确保所有异常都以一致的格式返回给客户端。
 * <p>details 字段用于承载结构化错误详情（如字段校验失败列表），为 null 时序列化自动忽略。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
public class ErrorResponse {

    /** 错误码，标识具体的错误类型 */
    private String code;

    /** 错误消息，描述具体的错误原因 */
    private String message;

    /** 错误详情，承载结构化错误信息，无详情时为 null */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object details;

    /**
     * 构造方法
     *
     * <h3>使用场景
     * <p>用于创建仅包含错误码和错误消息的异常响应对象。
     *
     * @param code    错误码，用于标识具体的错误类型
     * @param message 错误消息，提供对错误的详细描述信息
     */
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 构造方法
     *
     * <h3>使用场景
     * <p>用于创建包含错误码、错误消息和结构化错误详情的异常响应对象。
     *
     * @param code    错误码，用于标识具体的错误类型
     * @param message 错误消息，提供对错误的详细描述信息
     * @param details 错误详情，承载结构化错误信息，无详情时传 null
     */
    public ErrorResponse(String code, String message, Object details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }
}
