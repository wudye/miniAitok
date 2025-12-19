@EqualsAndHashCode(callSuper = true)

@EqualsAndHashCode(callSuper = true) 注解会为类自动生成 equals 和 hashCode 方法。callSuper = true 表示在生成这些方法时，
会调用父类的 equals 和 hashCode 方法。这在继承场景中很重要，确保父类的字段也被纳入比较和哈希计算中 give me an example

默认情况下 Lombok 生成的 equals/hashCode 不会调用父类。@Data 包含 @EqualsAndHashCode，
其默认参数是 callSuper = false，所以生成的方法只比较当前类的字段，忽略父类字段