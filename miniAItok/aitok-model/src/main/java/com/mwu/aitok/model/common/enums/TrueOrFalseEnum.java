package com.mwu.aitok.model.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * TrueOrFalseEnum
 *
 * @AUTHOR: mwu
 * @DATE: 2025/6/2
 **/
@Getter
@AllArgsConstructor
public enum TrueOrFalseEnum {
    TRUE("1", "true"),
    FALSE("0", "false"),
    ;

    private final String code;
    private final String info;

    public static TrueOrFalseEnum findByCode(String code) {
        for (TrueOrFalseEnum value : TrueOrFalseEnum.values()) {
            if (code.equals(value.getCode())) {
                return value;
            }
        }
        return null;
    }

    public static boolean isFalse(String code) {
        return Objects.equals(TRUE.code, code);
    }

    public static boolean isTrue(String code) {
        return Objects.equals(FALSE.code, code);
    }
}
