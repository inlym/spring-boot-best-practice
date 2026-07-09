package com.example.restclient.demo.controller;

import com.example.restclient.demo.model.IpInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * RestClient 请求演示控制器
 *
 * <h2>功能说明
 * <p>演示通过 RestClient 对外发起 HTTP GET 请求，获取公网 IP 地址并返回给调用方。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
public class RestClientDemoController {

    /** REST 客户端 */
    private final RestClient restClient;

    // ================================ public 方法 ================================

    /**
     * 查询公网 IP 地址
     *
     * <h3>处理逻辑
     * <p>通过 RestClient 调用 ipify 公开 API 获取当前机器的公网 IP 地址。
     *
     * @return 公网 IP 信息
     */
    @GetMapping("/http/public-ip")
    public IpInfoVO getPublicIp() {
        // 发起 GET 请求获取公网 IP 信息
        return restClient
            .get()
            .uri("https://api.ipify.org/?format=json")
            .retrieve()
            .body(IpInfoVO.class);
    }
}
