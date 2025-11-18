// java
package com.mwu.aitiokcoomon.core.utils.bean;

import com.mwu.aitiokcoomon.core.utils.CollectionUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * Bean 工具类（使用 Spring 的 BeanUtils 替代 hutool BeanUtil）
 */
public class BeanUtils {

    public static <T> T toBean(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        if (targetClass.isInstance(source)) {
            return targetClass.cast(source);
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            org.springframework.beans.BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Bean 转换失败", e);
        }
    }

    public static <T> T toBean(Object source, Class<T> targetClass, Consumer<T> peek) {
        T target = toBean(source, targetClass);
        if (target != null && peek != null) {
            peek.accept(target);
        }
        return target;
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        return CollectionUtils.convertList(source, s -> toBean(s, targetType));
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType, Consumer<T> peek) {
        List<T> list = toBean(source, targetType);
        if (list != null && peek != null) {
            list.forEach(peek);
        }
        return list;
    }

    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        org.springframework.beans.BeanUtils.copyProperties(source, target);
    }

}
