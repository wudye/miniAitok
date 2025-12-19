package com.mwu.aitiokcoomon.core.annotations;

import java.lang.annotation.*;

/**
 * MappingCostTime
 * 接口请求耗时统计
 *
 * @AUTHOR: mwu
 * @DATE: 2024/5/5
 **/
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingCostTime {
}
