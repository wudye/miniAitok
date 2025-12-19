// java
package com.mwu.aitiokcoomon.core.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.lang.Nullable;

/**
 * Spring 上下文持有器，替代 hutool 的 SpringUtil
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static volatile ApplicationContext CONTEXT;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.CONTEXT = applicationContext;
    }

    @Nullable
    public static <T> T getBean(Class<T> requiredType) {
        ApplicationContext ctx = CONTEXT;
        return ctx == null ? null : ctx.getBean(requiredType);
    }

    @Nullable
    public static Object getBean(String name) {
        ApplicationContext ctx = CONTEXT;
        return ctx == null ? null : ctx.getBean(name);
    }

    public static boolean containsBean(String name) {
        ApplicationContext ctx = CONTEXT;
        return ctx != null && ctx.containsBean(name);
    }

    public static void clear() {
        CONTEXT = null;
    }

}
