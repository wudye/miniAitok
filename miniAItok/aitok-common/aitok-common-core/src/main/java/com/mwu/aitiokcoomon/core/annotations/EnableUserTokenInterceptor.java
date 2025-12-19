package com.mwu.aitiokcoomon.core.annotations;


import com.mwu.aitiokcoomon.core.config.WebMvcConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EnableUserTokenInterceptor
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/30
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(WebMvcConfig.class)
public @interface EnableUserTokenInterceptor {
}
