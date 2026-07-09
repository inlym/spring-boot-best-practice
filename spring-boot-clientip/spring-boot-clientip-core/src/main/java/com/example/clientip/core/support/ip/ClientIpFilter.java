package com.example.clientip.core.support.ip;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 客户端 IP 过滤器
 *
 * <h2>说明
 * <p>从指定请求头中提取客户端真实 IP 地址，支持直接取值和逗号分割后按索引取值两种模式。
 *
 * <h2>使用示例
 * <p>Envoy 代理：headerName="x-envoy-external-address", directIp=true
 * <p>Nginx 代理：headerName="X-Forwarded-For", directIp=false, ipIndex=-2
 *
 * @author <a href="https://www.inlym.com">inlym</a>
 * @since 1.0.0
 */
@Slf4j
public class ClientIpFilter extends OncePerRequestFilter {

    /** 客户端 IP 请求属性键名 */
    private static final String CLIENT_IP = "clientIp";

    /** 客户端 IP 所在的请求头名称 */
    @Setter
    private String headerName;

    /** 请求头的值是否直接对应 IP 地址 */
    @Setter
    private boolean directIp;

    /** 请求头值按逗号分割后 IP 地址所在的索引，负数表示倒数第 n 个 */
    @Setter
    private int ipIndex;

    /**
     * 判断是否需要跳过过滤处理
     *
     * @param request HTTP 请求对象
     * @return true 表示跳过过滤处理，false 表示执行过滤处理
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String headerValue = request.getHeader(headerName);
        return !StringUtils.hasText(headerValue);
    }

    /**
     * 执行过滤器内部逻辑
     *
     * @param request     HTTP 请求对象
     * @param response    HTTP 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException 处理请求时发生 Servlet 异常
     * @throws IOException      处理请求时发生 IO 异常
     */
    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String headerValue = request.getHeader(headerName);

        // 根据配置从请求头值中提取客户端 IP
        String clientIp;
        if (directIp) {
            clientIp = headerValue;
        } else {
            String[] parts = headerValue.split(",");
            int actualIndex = ipIndex >= 0 ? ipIndex : parts.length + ipIndex;
            clientIp = parts[actualIndex].trim();
        }

        // 仅在请求属性中为空时才赋值，同时镜像到 MDC 供日志输出
        boolean shouldCleanMdc = false;
        if (request.getAttribute(CLIENT_IP) == null) {
            request.setAttribute(CLIENT_IP, clientIp);
            MDC.put(CLIENT_IP, clientIp);
            shouldCleanMdc = true;
            log.trace("从 {} 请求头获取客户端 IP 地址：{}", headerName, clientIp);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (shouldCleanMdc) {
                // 清理 MDC，避免 Servlet 容器线程复用导致客户端 IP 串到其他请求
                MDC.remove(CLIENT_IP);
            }
        }
    }
}
