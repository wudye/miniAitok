package com.mwu.aitok.service.video.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/*
Anti-Repeat Submit
适用场景
防重复提交：

表单提交
文件上传
支付操作
订单创建
Rate Limit
限流：

API访问频率控制
防止DDOS攻击
Retry
重试：

网络请求失败
数据库操作失败


 */
//@Inherited：允许子类继承父类方法上的该注解。
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
//@Documented：表明该注解会包含在生成的 Javadoc 中。
@Documented
public @interface VideoRepeatSubmit {

    String key() default "";

    /**
     * key过期时间，单位毫秒
     * 注解包含以下几个属性：
     * key 属性：用于指定一个唯一的键值，默认值为空字符串。这个键值通常用来标识某个操作或资源。
     * interval 属性：用于指定两次操作之间的最小时间间隔，默认值为 5 秒。
     * timeunit 属性：用于指定时间间隔的单位，默认值为 TimeUnit.MILLISECONDS。
     * message 属性：用于指定提示消息，默认值为 "您的操作太快了，请稍候再试"。
     */
    long interval() default 5000;

    TimeUnit timeunit() default TimeUnit.MILLISECONDS;

    /**
     * 提示消息
     */
    String message() default "您的操作太快了，请稍候再试";
}
