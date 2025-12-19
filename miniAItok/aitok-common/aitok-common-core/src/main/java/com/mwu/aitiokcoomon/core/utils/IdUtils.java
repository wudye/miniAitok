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

    /*

    ThreadLocalRandom 是 Java（自 Java 7 起）提供的一个每线程专用的伪随机数生成器，位于 java.util.concurrent 包中。主要特点和注意点：
目的：为多线程场景提供低争用、高性能的随机数生成。每个线程有自己的种子和状态，避免多个线程共享单一 Random 导致的竞争（锁/原子操作开销）。
使用方式：通过 ThreadLocalRandom.current() 获取当前线程的实例，然后调用 nextInt、nextLong、nextDouble 等方法。不要尝试通过构造器创建实例（构造器不可用/受保护）。
优势：在多线程并发生成随机数时，比共享的 java.util.Random 更快、延迟更低。
与其它类比较：
不适合需要可复现序列的场景（若需要可控种子，使用 Random 并显式传入种子）。
不适合加密场景（需用 SecureRandom）。
对于大量单线程/并行流生成随机数，SplittableRandom 也是一个高性能替代，尤其适合流式/可分割场景。
     */
    /**
     * 获取随机UUID，使用性能更好的 ThreadLocalRandom 生成 UUID
     *，返回一个标准格式的 UUID 字符串。UUID 的字符串格式通常为 8-4-4-4-12 的形式（例如：123e4567-e89b-12d3-a456-426614174000）
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
