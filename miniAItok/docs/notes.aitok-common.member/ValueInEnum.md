values() 是由 Java 编译器为每个枚举自动生成的静态方法，返回该枚举中所有枚举常量组成的数组。常见用法是使用增强的 for 循环遍历所有枚举实例，例如：
for (ShowStatusEnum s : ShowStatusEnum.values()) { ... }
下面给出两个简短文件示例：一个枚举定义（包含 findByCode，内部使用 values() 遍历），和一个演示类在 main 中打印所有枚举并按码查找的示例。
// file: ShowStatusEnum.java
public enum ShowStatusEnum {
HIDE("0", "隐藏"),
SHOW("1", "显示");

    private final String code;
    private final String desc;

    private ShowStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static ShowStatusEnum findByCode(String code) {
        for (ShowStatusEnum s : ShowStatusEnum.values()) {
            if (s.code.equals(code)) {
                return s;
            }
        }
        return null;
    }
}
// file: ShowStatusDemo.java
public class ShowStatusDemo {
public static void main(String[] args) {
// 遍历并打印所有枚举实例
for (ShowStatusEnum s : ShowStatusEnum.values()) {
System.out.println(s.getCode() + " -> " + s.getInfo());
}

        // 按 code 查找
        ShowStatusEnum found = ShowStatusEnum.findByCode("1");
        System.out.println("Found: " + found); // 输出 Found: SHOW
    }
}
