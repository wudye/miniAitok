package com.mwu.aitok.model.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * StateFlagEnum
 *
 * @AUTHOR: mwu
 * @DATE: 2025/6/2
 **/
@Getter
@AllArgsConstructor
public enum StateFlagEnum {
    ENABLE("0", "正常"),
    DISABLE("1", "禁用"),
    ;

    private final String code;
    private final String info;

    public static StateFlagEnum findByCode(String code) {
        for (StateFlagEnum value : StateFlagEnum.values()) {
            if (code.equals(value.getCode())) {
                return value;
            }
        }
        return null;
    }


    public static boolean isEnable(String status) {
        return Objects.equals(ENABLE.code, status);
    }

    public static boolean isDisable(String status) {
        return Objects.equals(DISABLE.code, status);
    }
}
