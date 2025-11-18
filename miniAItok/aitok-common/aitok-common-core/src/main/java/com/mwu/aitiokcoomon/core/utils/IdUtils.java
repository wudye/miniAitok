// java
package com.mwu.aitiokcoomon.core.utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ID生成器工具类
 *
 * @author mwu
 */
public class IdUtils {

    public static void main(String[] args) {
        System.out.println("simpleUUID() = " + simpleUUID());
    }

    /**
     * 获取随机UUID
     *
     * @return 随机UUID
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取随机UUID，默认32位，只截取前八位
     *
     * @return 随机UUID 的前8位
     */
    public static String shortUUID() {
        return UUID.randomUUID().toString().split("-")[0];
    }

    /**
     * 简化的UUID，去掉了横线
     *
     * @return 简化的UUID（不含短横线）
     */
    public static String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取随机UUID，使用性能更好的 ThreadLocalRandom 生成 UUID
     *
     * @return 随机UUID（含短横线）
     */
    public static String fastUUID() {
        UUID u = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong());
        return u.toString();
    }

    /**
     * 简化的 UUID，去掉了横线，使用性能更好的 ThreadLocalRandom 生成 UUID
     *
     * @return 简化的 UUID（不含短横线）
     */
    public static String fastSimpleUUID() {
        UUID u = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong());
        return u.toString().replace("-", "");
    }
}
