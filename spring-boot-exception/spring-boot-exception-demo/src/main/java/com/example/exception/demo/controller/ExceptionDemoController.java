package com.example.exception.demo.controller;

import com.example.exception.core.exception.BaseException;
import com.example.exception.core.exception.EntityNotFoundException;
import com.example.exception.core.exception.UnpredictableException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异常处理演示控制器
 *
 * <h2>功能说明
 * <p>通过不同的端点触发各类异常，演示全局异常处理器对异常的拦截与统一错误响应。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@RestController
public class ExceptionDemoController {

    // ================================ public 方法 ================================

    /**
     * 触发实体未找到异常
     *
     * <h3>预期响应
     * <p>HTTP 404，错误码 ENTITY_NOT_FOUND
     *
     * @return 无返回值，始终抛出异常
     */
    @GetMapping("/exceptions/entity-not-found")
    public void triggerEntityNotFound() {
        // 模拟查询实体不存在，抛出 EntityNotFoundException
        throw new EntityNotFoundException("用户不存在，userId=1");
    }

    /**
     * 触发意料之外异常
     *
     * <h3>预期响应
     * <p>HTTP 500，错误码 UNPREDICTABLE_ERROR
     *
     * @return 无返回值，始终抛出异常
     */
    @GetMapping("/exceptions/unpredictable")
    public void triggerUnpredictable() {
        // 模拟进入未预期的分支，抛出 UnpredictableException
        throw new UnpredictableException("支付状态异常，status=unknown");
    }

    /**
     * 触发业务异常基类
     *
     * <h3>预期响应
     * <p>HTTP 400，错误码 BUSINESS_ERROR
     *
     * @return 无返回值，始终抛出异常
     */
    @GetMapping("/exceptions/base")
    public void triggerBaseException() {
        // 直接抛出 BaseException，由通用的业务异常处理器拦截
        throw new BaseException("操作不被允许，当前状态为已结束");
    }

    /**
     * 触发未预期运行时异常
     *
     * <h3>预期响应
     * <p>HTTP 500，错误码 INTERNAL_ERROR
     *
     * @return 无返回值，始终抛出异常
     */
    @GetMapping("/exceptions/generic")
    public void triggerGenericException() {
        // 模拟空指针异常，由兜底的 Exception 处理器拦截
        String value = null;
        value.length();
    }
}
