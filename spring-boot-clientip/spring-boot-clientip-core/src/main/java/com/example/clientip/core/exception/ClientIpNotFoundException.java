package com.example.clientip.core.exception;

/**
 * 客户端 IP 地址未找到异常
 *
 * <h2>说明
 * <p>当从请求属性中无法获取客户端 IP 地址时抛出，通常表示过滤器链配置异常或请求未经过 ClientIpFilter 处理。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
public class ClientIpNotFoundException extends RuntimeException {

    /**
     * 构造异常实例
     *
     * @param message 异常描述信息
     */
    public ClientIpNotFoundException(String message) {
        super(message);
    }
}
