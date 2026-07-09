package com.example.clientip.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户端 IP 信息响应模型
 *
 * <h2>说明
 * <p>封装通过 X-Forwarded-For 请求头提取的客户端真实 IP 地址。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientIpInfoVO {

    /** 客户端 IP 地址 */
    private String clientIp;
}
