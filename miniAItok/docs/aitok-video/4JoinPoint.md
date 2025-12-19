JoinPoint（通常是 org.aspectj.lang.JoinPoint）表示切面拦截到的“连接点”，即被拦截的那个方法调用。它在通知方法中作为参数提供，常用于读取被调用方法的上下文信息。常用方法：
getArgs()：返回方法参数数组。
getTarget()：返回被代理的目标对象（实现类实例）。
getThis()：返回代理对象自身。
getSignature()：返回方法签名（通常需要转为 MethodSignature 来取 Method）。
getStaticPart()：返回静态连接点信息。
注意：在 @Around 通知中一般使用 ProceedingJoinPoint（继承自 JoinPoint），可以调用 proceed() 执行被拦截方法。
示例（Java）说明如何在 @Before 通知中使用 JoinPoint：

// java
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import java.lang.reflect.Method;

public void beforeAdvice(JoinPoint point) throws NoSuchMethodException {
// 参数
Object[] args = point.getArgs();

    // 目标对象（实现类实例）
    Object target = point.getTarget();

    // 签名 -> Method
    MethodSignature sig = (MethodSignature) point.getSignature();
    Method method = sig.getMethod(); // 可能是接口方法，若需实现类方法可用 target.getClass().getMethod(...)

    // 示例输出
    System.out.println("method: " + method.getName());
    System.out.println("target class: " + target.getClass().getName());
    System.out.println("args: " + java.util.Arrays.toString(args));
}
