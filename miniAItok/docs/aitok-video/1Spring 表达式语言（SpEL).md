    Spring 表达式语言（SpEL）是 Spring 提供的一种功能强大的表达式语言，用于在运行时对对象图、方法调用、属性访问、集合筛选、算术/逻辑运算等进行动态求值。常见用途包括在注解（如 @Value、缓存 `key` 表达式）、安全表达式、条件化配置以及自定义工具代码中动态计算字符串或键值。
    要点：
    语法支持属性访问（person.name）、方法调用（person.getName()）、算术与逻辑运算（1 + 2 > 2）、三元运算、集合选择/投影（list.?[age > 18] / list.![name]）。
    可以通过上下文变量（用 #varName 引用）绑定外部数据，例如方法参数或自定义变量。
    可编程使用 SpelExpressionParser 与 StandardEvaluationContext 来解析和求值，或在 Spring 注解/配置中直接使用字符串表达式。

    // java
    ExpressionParser parser = new SpelExpressionParser();
    StandardEvaluationContext ctx = new StandardEvaluationContext();
ctx.setVariable("id", 42);
String result = parser.parseExpression("#id + 1").getValue(ctx, String.class); // "43"
简短总结：SpEL 是在 Spring 应用中进行动态计算和条件判断的通用工具，适合需要在运行时基于上下文生成值（如缓存 key、消息路由、配置开关等）的场景。