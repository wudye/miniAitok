
核心区别总结
概念	类型	主要用途	获取方式
JoinPoint	AOP接口	表示程序执行点，包含方法调用上下文	AOP切面方法参数
MethodSignature	AOP接口	专门表示方法签名信息	joinPoint.getSignature()
Method	反射类	Java反射API中的方法对象	methodSignature.getMethod()
使用场景：
    JoinPoint - 在切面中获取目标对象、参数等运行时信息
    MethodSignature - 获取方法名、参数类型、返回类型等签名信息
    Method - 进行反射操作，如动态调用方法、获取注解等
MethodSignature
MethodSignature 是Spring AOP中表示方法签名的接口，它继承自 Signature
    // 主要方法：
    String getName()           // 方法名
    Class<?> getReturnType()   // 返回类型
    Class<?>[] getParameterTypes() // 参数类型数组
    String[] getParameterNames()   // 参数名数组
    Method getMethod()         // 获取实际的Method对象
Method
Method 是Java反射API中表示方法的类：
    // 主要方法：
    String getName()                    // 方法名
    Class<?> getReturnType()           // 返回类型  
    Class<?>[] getParameterTypes()     // 参数类型
    Object invoke(Object obj, Object... args) // 调用方法
    int getModifiers()                 // 修饰符
JoinPoint
JoinPoint 是AOP中的连接点，表示程序执行过程中的特定点（如方法调用）：
    // 主要方法：
    Object getTarget()              // 目标对象
    Object[] getArgs()              // 方法参数
    Signature getSignature()        // 获取签名
    Object getThis()                // 代理对象