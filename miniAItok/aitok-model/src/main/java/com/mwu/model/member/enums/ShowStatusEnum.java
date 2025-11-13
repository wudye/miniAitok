package com.mwu.model.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ShowStatusEnum
 *
 * @author mwu
 * @since  2025/11/13
 */
@Getter
@AllArgsConstructor
public enum ShowStatusEnum {


    SHOW("0", "display"),
    HIDE("1", "hide");

    private final String code;
    private final String info;


    public static ShowStatusEnum findByCode(String code) {
        for (ShowStatusEnum value : ShowStatusEnum.values()) {
            if (code.equals(value.getCode())) {
                return value;
            }
        }
        return null;
    }

}
