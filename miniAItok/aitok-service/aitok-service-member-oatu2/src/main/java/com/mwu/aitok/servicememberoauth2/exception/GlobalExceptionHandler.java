package com.mwu.aitok.servicememberoauth2.exception;

import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理限流异常
     */
//    @ExceptionHandler(RuntimeException.class)
//    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
//    public R<Void> handleRateLimitException(RuntimeException e) {
//        if (e.getMessage().contains("请求过于频繁")) {
//            log.warn("Rate limit triggered: {}", e.getMessage());
//            return R.fail(HttpCodeEnum.TOO_MANY_REQUESTS.getCode(), e.getMessage());
//        }
//
//        // 其他RuntimeException
//        log.error("Runtime exception: ", e);
//        return R.fail(HttpCodeEnum.SYSTEM_ERROR.getCode(), "ratelimite error，请稍后重试");
//    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e) {
        log.error("System error: ", e);
        return R.fail(HttpCodeEnum.SYSTEM_ERROR.getCode(), "common error happens，请稍后重试");
    }
}