package com.mwu.model.member.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * login enum type
 *
 * @author mwu
 * @since  2025/11/13
 */
@Getter
@AllArgsConstructor
public enum LoginTypeEnum {

    UP("0", "Username and Password login", "upLoginStrategy"),
    EMAIL("1", "Email login", "emailLoginStrategy"),
    MOBILE("2", "Mobile phone login", "mobileLoginStrategy");

    private final String code;
    private final String info;
    private final String strategy;

    public static ShowStatusEnum findByCode(String code) {
        for (ShowStatusEnum item : ShowStatusEnum.values()) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
