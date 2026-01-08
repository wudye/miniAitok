反射（Reflection）是 Java 在运行时检查和操作类、方法、字段、构造器及注解的机制（主要在 java.lang.Class 与 java.lang.reflect 包中）。它允许程序在运行时动态地：
获取类的元信息（类名、父类、接口、方法、字段、注解等）。
创建对象实例、调用方法、读写字段（包括私有成员，需 setAccessible(true)）。
实现框架功能（依赖注入、AOP、序列化、ORM、动态代理等）。
优点：灵活、支持通用框架设计；缺点：性能开销、可能破坏封装、可维护性和安全性问题。通常在框架层或工具中使用，并结合缓存等手段降低开销。
下面示例演示如何通过反射创建实例、调用方法、访问私有字段：
解释：示例先用 Class.forName 获取 Class，通过 getConstructor 创建实例，getMethod 调用公有方法，getDeclaredField + setAccessible(true) 访问私有字段，并用 invoke 执行方法。

// java
public class Person {
private String name;
public Person(String name) { this.name = name; }
public String greet(String who) { return name + " says hi to " + who; }
}

public class ReflectionExample {
public static void main(String[] args) throws Exception {
// 获取 Class 对象
Class<?> cls = Class.forName("Person");

        // 创建实例
        Object p = cls.getConstructor(String.class).newInstance("Alice");

        // 调用公有方法
        java.lang.reflect.Method m = cls.getMethod("greet", String.class);
        Object res = m.invoke(p, "Bob");
        System.out.println(res); // Alice says hi to Bob

        // 访问私有字段
        java.lang.reflect.Field f = cls.getDeclaredField("name");
        f.setAccessible(true);
        System.out.println("name = " + f.get(p)); // name = Alice
        f.set(p, "Carol");
        System.out.println(m.invoke(p, "Dave")); // Carol says hi to Dave
    }
}
