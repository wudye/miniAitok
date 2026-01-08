package com.mwu.aitiokcoomon.core.utils.spring;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Optional;

public class SpElUtils {
    private static final ExpressionParser parser = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /*
    把方法的参数按参数名绑定到 SpEL 上下文中，然后解析你传入的 SpEL 表达式并返回解析结果（String）。因此它可以用来把参数“组合”为字符串（例如 #userId + '_' + #count），也可以做更复杂的 SpEL 计算或方法调用。
参数名通过 DefaultParameterNameDiscoverer 获取，编译时需要保留参数名（例如使用 -parameters），否则变量名可能不可用。
在表达式中使用 #paramName 访问对应参数值。
支持所有 SpEL 功能（算术、字符串、调用静态方法、集合操作等）

// java
// 说明：从 Method 和 args 解析表达式 "#userId + '_' + #count"
Method method = ExampleService.class.getMethod("process", String.class, int.class);
Object[] args = new Object[] { "user42", 3 };
String result = SpElUtils.parseSpEl(method, args, "#userId + '_' + #count");
System.out.println(result); // 输出: user42_3

     */
    public static String parseSpEl(Method method, Object[] args, String spEl) {
        //解析参数名
        // 用于在运行时发现方法的参数名。它通过反射和字节码分析（如基于 -parameters 编译选项）来获取方法参数的名称
        String[] params = Optional.ofNullable(parameterNameDiscoverer.getParameterNames(method)).orElse(new String[]{});
        /*

        valuationContext 是 Spring 表达式语言（SpEL）在解析与求值时使用的上下文接口；StandardEvaluationContext 是它的常用实现。该上下文用于：
提供变量（setVariable）和根对象（setRootObject）给表达式使用。
注册自定义函数、类型转换器、属性/方法解析器、Bean 解析器等。
在你的代码里，它把方法参数以变量形式放入上下文，使 SpEL 能通过 #paramName 读取这些参数的值。
         */
        EvaluationContext context = new StandardEvaluationContext();//el解析需要的上下文对象
        for (int i = 0; i < params.length; i++) {
            context.setVariable(params[i], args[i]);//所有参数都作为原材料扔进去
        }
        Expression expression = parser.parseExpression(spEl);

        /*

            ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 把方法参数作为变量放入上下文
        context.setVariable("userId", "user42");
        context.setVariable("count", 3);

        // 解析并求值（可以使用 #userId 和 #count）
        Expression exp = parser.parseExpression("#userId + '_' + #count");
        String result = exp.getValue(context, String.class);

        System.out.println(result); // 输出: user42_3
         */
        return expression.getValue(context, String.class);
    }

    public static String getMethodKey(Method method) {
        return method.getName() + "_" + method.getName();
    }
}
