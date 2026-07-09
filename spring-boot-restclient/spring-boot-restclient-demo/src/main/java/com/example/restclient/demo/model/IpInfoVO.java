package com.example.restclient.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公网 IP 信息响应模型
 *
 * <h2>说明
 * <p>封装 ipify 服务返回的公网 IP 地址信息。
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpInfoVO {

    /** IP 地址 */
    private String ip;
}
