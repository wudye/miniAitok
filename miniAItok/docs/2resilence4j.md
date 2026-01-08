针对“服务雪崩”的情况，我们需要进行“服务容错”处理。解决的方向很“简单”，尽量不要去调用故障的服务，避免被拖垮。一般常用的手段有，主要是限流和开关。

① 限流

通过限制调用服务的频率，避免频繁调用故障服务，导致请求任务积压而自身雪崩。

② 开关

通过关闭对故障服务的调用，停止调用故障服务，从而避免服务雪崩。当然，关闭的前提是，不调用故障服务的情况下，业务逻辑依然可以走下去，或者业务数据的完整性不会被破坏。

自动开关比较经典的就是“断路器模式”，它源于 Martin Fowler 大佬在 《CircuitBreaker》 文章的分享。

“断路器”，又称自动开关，它是一种既有手动开关作用，又能自动进行失压、欠压、过载、和短路保护的电器。

它可用来分配电能，不频繁地启动异步电动机，对电源线路及电动机等实行保护，**当它们发生严重的过载或者短路及欠压等故障时能自动切断电路，其功能相当于熔断器式开关与过欠热继电器等的组合。**而且在分断故障电流后一般不需要变更零部件，一获得了广泛的应用。

在微服务架构中，“断路器模式”的用途也是类似的。当某个服务提供者发生故障（相当于电器发生短路的情况）时，断路器一旦监控到这个情况，会将开关进行自动关闭。之后，在服务消费者调用该故障服务提供者时，直接抛出错误异常，不进行调用，从而避免调用服务的漫长等待。

CircuitBreaker 一共有 CLOSED、OPEN、HALF_OPEN 三种状态，通过状态机实现。转换关系如下图所示：

    [!Circuit Breaker States](./state.jpg)


Resilience4j 采用的是 Ring Bit Buffer(环形缓冲区)。Ring Bit Buffer 在内部使用 BitSet 这样的数据结构来进行存储 
    [!Ring Bit Buffe](./ringBitBuffer.jpg)

当故障率高于设定的阈值时，熔断器状态会从由 CLOSE 变为 OPEN。这时所有的请求都会抛出 CallNotPermittedException 异常。
当经过一段时间后，熔断器的状态会从 OPEN 变为 HALF_OPEN。HALF_OPEN 状态下同样会有一个 Ring Bit Buffer，用来计算HALF_OPEN 状态下的故障率。如果高于配置的阈值，会转换为 OPEN，低于阈值则装换为 CLOSE。与 CLOSE 状态下的缓冲区不同的地方在于，HALF_OPEN 状态下的缓冲区大小会限制请求数，只有缓冲区大小的请求数会被放入 。

除此以外，熔断器还会有两种特殊状态：DISABLED（始终允许访问）和 FORCED_OPEN（始终拒绝访问）。这两个状态不会生成熔断器事件（除状态装换外），并且不会记录事件的成功或者失败。退出这两个状态的唯一方法是触发状态转换或者重置熔断器。

RateLimiter 一共有两种实现类：

AtomicRateLimiter：基于令牌桶限流算法实现限流。
SemaphoreBasedRateLimiter：基于 Semaphore 实现限流。

RateLimiter 一共有两种实现类：

AtomicRateLimiter：基于令牌桶限流算法实现限流。
SemaphoreBasedRateLimiter：基于 Semaphore 实现限流。
默认情况下，采用 AtomicRateLimiter 基于令牌桶限流算法实现限流。= = 搜了一圈 Resilience4j 的源码，貌似 SemaphoreBasedRateLimiter 没有地方在使用，难道已经被抛弃了~


Bulkhead 指的是船舶中的舱壁，它将船体分隔为多个船舱，在船部分受损时可避免沉船。

在 Resilience4j 中，提供了基于 Semaphore 信号量和 ThreadPool 线程池两种 Bulkhead 实现，隔离不同种类的调用，并提供流控的能力，从而避免某类调用异常时而占用所有资源，导致影响整个系统




Retry > Bulkhead > RateLimiter > TimeLimiter > Bulkhead


@CircuitBreaker(name = BACKEND, fallbackMethod = "fallback")
@RateLimiter(name = BACKEND)
@Bulkhead(name = BACKEND)
@Retry(name = BACKEND, fallbackMethod = "fallback")
@TimeLimiter(name = BACKEND)
public String method(String param1) {
throws new Exception("xxxx");
}

private String fallback(String param1, IllegalArgumentException e) {
return "test:IllegalArgumentException";
}

private String fallback(String param1, RuntimeException e) {
return "test:RuntimeException";
}


注解	切面	顺序
@Retry	RetryAspect	Ordered.LOWEST_PRECEDENCE - 4
@CircuitBreaker	CircuitBreakerAspect	Ordered.LOWEST_PRECEDENCE - 3
@RateLimiter	RateLimiterAspect	Ordered.LOWEST_PRECEDENCE - 2
@TimeLimiter	TimeLimiterAspect	Ordered.LOWEST_PRECEDENCE - 1
@Bulkhead	BulkheadAspect	Ordered.LOWEST_PRECEDENCE



https://www.iocoder.cn/Spring-Boot/Resilience4j/?self
[!Resilience4j Modules](./ratechoose.jpg)