package com.example.clientip.demo.controller;

import com.example.clientip.core.annotation.ClientIp;
import com.example.clientip.demo.model.ClientIpInfoVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户端 IP 地址演示控制器
 *
 * <h2>功能说明
 * <p>演示通过 @ClientIp 注解在控制器方法中注入客户端 IP 地址，并将其作为响应数据返回。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@RestController
public class ClientIpDemoController {

    // ================================ public 方法 ================================

    /**
     * 获取客户端 IP 地址
     *
     * <h3>处理逻辑
     * <p>通过 @ClientIp 注解自动注入从 X-Forwarded-For 请求头中提取的客户端 IP 地址，封装为响应模型返回。
     *
     * @param clientIp 客户端 IP 地址，由 ClientIpFilter 从请求头提取后经 ClientIpMethodArgumentResolver 注入
     * @return 客户端 IP 信息
     */
    @GetMapping("/client-ip")
    public ClientIpInfoVO getClientIp(@ClientIp String clientIp) {
        // 将注入的客户端 IP 封装为响应模型返回
        return ClientIpInfoVO.builder()
            .clientIp(clientIp)
            .build();
    }
}
