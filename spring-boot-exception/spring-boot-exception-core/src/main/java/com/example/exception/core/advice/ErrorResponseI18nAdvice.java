package com.example.exception.core.advice;

import com.example.exception.core.model.response.ErrorResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 错误响应多语言消息处理 advice
 *
 * <h2>说明
 * <p>拦截 ErrorResponse 类型响应，根据错误码从 MessageSource 中查找对应的多语言文案并替换。
 * <p>MessageSource 中未找到对应文案时保持原始消息不变，不阻塞正常响应流程。
 *
 * @author inlym
 * @since 4.1.0
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorResponseI18nAdvice implements ResponseBodyAdvice<ErrorResponse> {

    /** 消息源，用于获取多语言文案 */
    private final MessageSource messageSource;

    // ================================ public 方法 ================================

    @Override
    public boolean supports(
        @NonNull MethodParameter returnType,
        @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return ErrorResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public ErrorResponse beforeBodyWrite(
        @Nullable ErrorResponse body,
        @NonNull MethodParameter returnType,
        @NonNull MediaType selectedContentType,
        @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
        @NonNull ServerHttpRequest request,
        @NonNull ServerHttpResponse response
    ) {
        // 响应体为 null 时不做处理
        if (body == null) {
            log.trace("响应体为 null，跳过多语言处理");
            return null;
        }

        // 错误码为空时不做处理
        if (!StringUtils.hasText(body.getCode())) {
            log.trace("错误码为空，跳过多语言处理");
            return body;
        }

        // 将错误码转换为消息 key 并从消息源获取对应语言的文案，key 不存在时返回空字符串降级
        String messageKey = "response.error." + body.getCode().toLowerCase();
        String i18nMessage = messageSource.getMessage(messageKey, null, "", LocaleContextHolder.getLocale());

        // 消息源中存在对应文案时替换原 message，不存在时保持 GlobalExceptionHandler 的原始消息
        if (StringUtils.hasText(i18nMessage)) {
            log.trace("错误码 {} 的消息已替换为 {} 语言版本", body.getCode(), LocaleContextHolder.getLocale());
            body.setMessage(i18nMessage);
        } else {
            log.trace("错误码 {} 的多语言文案缺失，保持原始消息", body.getCode());
        }

        return body;
    }
}
