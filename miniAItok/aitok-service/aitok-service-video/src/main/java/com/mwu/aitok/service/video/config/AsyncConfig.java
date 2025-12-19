package com.mwu.aitok.service.video.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;
/*
1. 默认线程池的局限性
Spring 默认使用 SimpleAsyncTaskExecutor ，每次都创建新线程
没有线程复用，性能差
没有队列缓冲，容易导致任务丢
2.  线程池优化建议
把耗时任务异步化，避免阻塞请求线程，提高响应速度。
可通过配置控制核心/最大线程数及队列，适配不同负载。
支持优雅停机（等待任务完成）和未捕获异常处理。
耗时操作：视频转码、截图、上传等
高并发需求：多个用户同时上传视频
资源控制：避免无限制创建线程导致系统崩溃
 */
@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    /*
    coreSize=32：基于CPU核心数设计，保证基础处理能力
maxSize=64：峰值时可以扩展到64个线程
queueCapacity=100：100个任务缓冲队列，平衡内存和性
     */
    @Value("${video-async-executor.core-size}")
    private int coreSize;

    @Value("${video-async-executor.max-size}")
    private int maxSize;

    @Value("${video-async-executor.queue-capacity}")
    private int queueCapacity;

    /**
     * video-service 项目共用线程池
     */
    public static final String VIDEO_EXECUTOR = "videoAsyncExecutor";
    public static final String VIDEO_EXECUTOR_PREFIX = "video-async-executor-";

    @Override
    public TaskExecutor getAsyncExecutor() {
        return videoAsyncExecutor();
    }

    /*
    可以重复使用这些线程。简要说明要点：
ThreadPoolTaskExecutor 封装了 JDK 的 ThreadPoolExecutor，线程是被复用的：已创建的线程会在完成任务后返回到线程池，等待下一次任务复用，避免每次都新建线程。
corePoolSize 保留的核心线程数（长期存活，可复用），当任务超过队列且线程数小于 maxPoolSize 时，会创建新线程直到 maxPoolSize。
queueCapacity 用于缓冲待执行任务；队列满后才会尝试扩展线程数到 maxPoolSize。
CallerRunsPolicy 在线程池和队列都已饱和时，由调用线程执行任务（作为降级策略），避免任务丢失。
调用 initialize() 后线程池就会就绪；setWaitForTasksToCompleteOnShutdown(true) 可在关闭时等待任务完成。

     */
    @Bean(name = VIDEO_EXECUTOR, destroyMethod = "shutdown")
    @Primary
    public ThreadPoolTaskExecutor videoAsyncExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(VIDEO_EXECUTOR_PREFIX);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 新增建议配置（Spring Boot 3 优化项）
//        executor.setTaskDecorator(new MDCTaskDecorator()); // 支持MDC上下文传递
        executor.setWaitForTasksToCompleteOnShutdown(true); // 优雅停机等待任务完成
        executor.setAwaitTerminationSeconds(30); // 等待超时时间


        executor.initialize();
        log.info("Video async executor initialized: core={}, max={}, queue={}", coreSize, maxSize, queueCapacity);
        return executor;

    }




    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

}
