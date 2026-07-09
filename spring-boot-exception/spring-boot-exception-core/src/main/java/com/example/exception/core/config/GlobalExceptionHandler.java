package com.example.exception.core.config;

import com.example.exception.core.exception.BaseException;
import com.example.exception.core.exception.EntityNotFoundException;
import com.example.exception.core.exception.UnpredictableException;
import com.example.exception.core.model.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * <h2>类说明
 * <p>统一捕获应用程序中的各类异常，并将异常信息转换为标准的错误响应。
 * <p>通过 @RestControllerAdvice 注解实现全局异常拦截，确保所有异常都能被妥善处理并返回统一格式的错误信息。
 * <p>使用最低优先级，确保模块级异常处理器优先于本处理器执行。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理实体未找到异常
     *
     * <h3>处理逻辑
     * <p>当查询实体未找到或归属校验不匹配时触发。
     * <p>出于安全考虑，两种情况统一返回相同响应，避免泄露实体归属信息。
     *
     * @param e 实体未找到异常
     * @return 错误响应
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public ErrorResponse handleEntityNotFound(EntityNotFoundException e) {
        log.trace("实体未找到: {}", e.getMessage());
        return new ErrorResponse("ENTITY_NOT_FOUND", "请求的资源不存在或已被删除");
    }

    /**
     * 处理意料之外异常
     *
     * <h3>处理逻辑
     * <p>当分支判断中出现未考虑到的情况时触发，表示程序进入了预期之外的执行路径。
     *
     * @param e 意料之外异常
     * @return 错误响应
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(UnpredictableException.class)
    public ErrorResponse handleUnpredictable(UnpredictableException e) {
        log.error("意料之外异常", e);
        return new ErrorResponse("UNPREDICTABLE_ERROR", "系统处理异常，请稍后重试");
    }

    /**
     * 处理业务异常
     *
     * <h3>处理逻辑
     * <p>作为业务异常的统一处理入口，捕获所有未被更具体的处理器匹配的 BaseException 子类。
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BaseException.class)
    public ErrorResponse handleBaseException(BaseException e) {
        log.trace("业务异常: {}", e.getMessage());
        return new ErrorResponse("BUSINESS_ERROR", "操作失败，请稍后重试");
    }

    /**
     * 处理其他未捕获的异常
     *
     * <h3>处理逻辑
     * <p>作为最后的异常处理器，捕获所有未被上述方法处理的其他异常。
     *
     * @param e 通用异常
     * @return 错误响应
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleGenericException(Exception e) {
        log.error("未处理的异常", e);
        return new ErrorResponse("INTERNAL_ERROR", "服务器繁忙，请稍后重试");
    }
}
