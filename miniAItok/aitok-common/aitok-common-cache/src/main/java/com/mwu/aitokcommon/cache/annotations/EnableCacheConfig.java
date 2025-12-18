package com.mwu.aitokcommon.cache.annotations;


import com.mwu.aitokcommon.cache.aspect.DubboCacheAspect;
import com.mwu.aitokcommon.cache.aspect.RedissonLockAspect;
import com.mwu.aitokcommon.cache.config.CaffeineConfig;
import com.mwu.aitokcommon.cache.config.RedisConfig;
import com.mwu.aitokcommon.cache.service.LockService;
import com.mwu.aitokcommon.cache.service.RedisService;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EnableRedisConfig
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/30
 **/
/*
@Target({ElementType.TYPE})

指定此注解只能应用于类级别
通常用在主启动类或配置类上
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
/*
核心功能：自动导入6个配置类
这是Spring的配置导入机制，简化了Bean的注册过程
 */
@Import({RedisConfig.class, CaffeineConfig.class, RedisService.class, RedissonLockAspect.class, DubboCacheAspect.class, LockService.class})
public @interface EnableCacheConfig {
}

/*
already use the starter autoconfiguration
so this annotation is deprecated now

同时使用：

@EnableCacheConfig (通过 @Import 导入)
AutoConfiguration.imports (自动配置)
结果：Spring Boot 会创建两份相同的 Bean 定义，但不会报错，因为：

Spring 容器会使用 @Primary 或 @ConditionalOnMissingBean 来选择最终实例
你的 RedisConfig 中的 @Bean 方法有 @Primary 注解，会被优先选择
3. 潜在问题
重复实例化：虽然最终只有一个实例生效，但 Spring 会处理重复定义
配置覆盖：如果两个地方定义了不同的配置，可能会有意外的行为
建议的最佳实践
方案1：只使用自动配置（推荐）
case2: 只使用 @EnableCacheConfig
（不推荐）
避免重复定义，确保配置集中管理
完全可以删除 AutoConfiguration.imports ，只保留 @EnableCacheConfig 的 @Import 方式。这确实是构建 Spring Boot Starter 的一种常见且有效的方式。
but Spring Boot Starter 最佳实践
推荐的 Starter 结构
aitok-cache-starter/
├── src/main/java/
│   └── com/mwu/aitok/cache/
│       ├── autoconfigure/
│       │   └── CacheAutoConfiguration.java  # 自动配置类
│       ├── annotations/
│       │   └── EnableCacheConfig.java       # 手动启用注解
│       └── properties/
│           └── CacheProperties.java          # 配置属性
├── src/main/resources/
│   └── META-INF/
│       ├── spring.factories                   # 传统方式
│       └── spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports  # 新方式
└── pom.xml

 */