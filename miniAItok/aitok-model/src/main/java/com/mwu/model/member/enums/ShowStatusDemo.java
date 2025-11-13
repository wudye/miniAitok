package com.mwu.model.member.enums;

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
