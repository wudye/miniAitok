ThreadLocalRandom

ThreadLocalRandom 是 Java（自 Java 7 起）提供的一个每线程专用的伪随机数生成器，位于 java.util.concurrent 包中。主要特点和注意点：
目的：为多线程场景提供低争用、高性能的随机数生成。每个线程有自己的种子和状态，避免多个线程共享单一 Random 导致的竞争（锁/原子操作开销）。
使用方式：通过 ThreadLocalRandom.current() 获取当前线程的实例，然后调用 nextInt、nextLong、nextDouble 等方法。不要尝试通过构造器创建实例（构造器不可用/受保护）。
优势：在多线程并发生成随机数时，比共享的 java.util.Random 更快、延迟更低。
与其它类比较：
不适合需要可复现序列的场景（若需要可控种子，使用 Random 并显式传入种子）。
不适合加密场景（需用 SecureRandom）。
对于大量单线程/并行流生成随机数，SplittableRandom 也是一个高性能替代，尤其适合流式/可分割场景。
java
int n = ThreadLocalRandom.current().nextInt(0, 100);       // 0..99
long l = ThreadLocalRandom.current().nextLong(0, 1_000L);  // 0..999
double d = ThreadLocalRandom.current().nextDouble();       // 0.0 .. 1.0


SecureRandom 是 Java 提供的加密安全伪随机数生成器（CSPRNG），位于 java.security 包。它从操作系统的熵源或指定的算法获取随机性，适合用于生成密钥、IV、nonce、密码盐、认证令牌等安全相关场景。要点：
强随机性：比 Random 或 ThreadLocalRandom 更适合加密用途。
获取方式：new SecureRandom()（自动种子）或 SecureRandom.getInstanceStrong()（更强但可能阻塞）。
性能/阻塞：getInstanceStrong() 在某些平台上可能阻塞直到有足够熵；new SecureRandom() 通常是非阻塞且足够用。
不适合可重复测试：若需要可重现序列，应显式使用可控种子或其他确定性 PRNG。
常用用途：生成密钥、盐、一次性 token、随机 IV 等。


UUID.randomUUID()
使用加密强伪随机数（实现上使用 SecureRandom），会生成符合 RFC4122 的 v4 UUID（正确设置了 version/variant 位）。
安全性高，但在高并发或频繁生成时比非加密随机慢一些。
推荐用于需要唯一且不可预测的 ID（比如 token、密钥、证书等）。
ThreadLocalRandom
为多线程场景优化的高性能伪随机生成器（非加密），没有加锁竞争。
速度快，适合大量并发生成随机数的场景（性能优先、非安全需求）。
直接用 ThreadLocalRandom 生成两个 long 并 new UUID(msb, lsb) 时，默认不保证 RFC4122 的 version/variant 位；需要手动设置位来形成合法的 v4 UUID。
选择建议
需要不可预测/安全性 -> 用 UUID.randomUUID()（或 SecureRandom）。
仅需高性能、唯一性概率足够 -> 可用 ThreadLocalRandom，但注意设置 version/variant 或直接用生成的字符串做非标准 ID。