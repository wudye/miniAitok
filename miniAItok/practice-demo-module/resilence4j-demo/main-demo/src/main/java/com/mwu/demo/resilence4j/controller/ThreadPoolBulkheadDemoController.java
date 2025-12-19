package com.mwu.demo.resilence4j.controller;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/thread-pool-bulkhead-demo")
public class ThreadPoolBulkheadDemoController {

    @Autowired
    private ThreadPoolBulkheadService threadPoolBulkheadService;

    @GetMapping("/get_user")
    public String getUser(@RequestParam("id") Integer id) throws ExecutionException, InterruptedException {
        threadPoolBulkheadService.getUser0(id);
        return threadPoolBulkheadService.getUser0(id).get();
    }


    /*
    这里创建了 ThreadPoolBulkheadService 的原因是，这里我们使用 Resilience4j 是基于注解 + AOP的方式，如果直接 this
    . 方式来调用方法，实际没有走代理，导致 Resilience4j 无法使用 AOP。
    所以这里创建了 ThreadPoolBulkheadService，将方法调用委托给 ThreadPoolBulkheadService

    线程池隔离（ThreadPool Bulkhead）通过把调用放到一个受限大小的线程池和队列里来限制并发，避免某个慢/阻塞的依赖耗尽主线程资源。
在注解方式下，使用 @Bulkhead(name = "backendD", type = Bulkhead.Type.THREADPOOL, fallbackMethod = "getUserFallback")
标记返回 CompletableFuture 的方法；当线程池和队列已满或调用失败时，Resilience4j 会触发回退方法（签名和返回类型必须匹配，回退方法可多一个 Throwable 参数）。
注意不要用 this 直接调用被注解的方法；应通过 Spring 注入的 bean（如 ThreadPoolBulkheadService）以触发 AOP 代理。
     */
    @Service
    public static class ThreadPoolBulkheadService {

        private Logger logger = LoggerFactory.getLogger(ThreadPoolBulkheadService.class);

        @Bulkhead(name = "backendD", fallbackMethod = "getUserFallback", type = Bulkhead.Type.THREADPOOL)
        public CompletableFuture<String> getUser0(Integer id) throws InterruptedException {
            logger.info("[getUser][id({})]", id);
            Thread.sleep(10 * 1000L); // sleep 10 秒
            return CompletableFuture.completedFuture("User:" + id);
        }

        public CompletableFuture<String> getUserFallback(Integer id, Throwable throwable) {
            logger.info("[getUserFallback][id({}) exception({})]", id, throwable.getClass().getSimpleName());
            return CompletableFuture.completedFuture("mock:User:" + id);
        }

    }


}
