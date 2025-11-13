package com.mwu.model.member.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * register body
 *
 *  @author mwu
 *  @since  2025/11/13
 */
// @EqualsAndHashCode(callSuper = true) 注解会为类自动生成 equals 和 hashCode 方法。callSuper = true 表示在生成这些方法时，
// 会调用父类的 equals 和 hashCode 方法。这在继承场景中很重要，确保父类的字段也被纳入比较和哈希计算中
@EqualsAndHashCode(callSuper = true)
@Data
public class RegisterBody extends LoginUserDTO{

    /**
     * password confirmation
     */
    private String confirmPassword;
}