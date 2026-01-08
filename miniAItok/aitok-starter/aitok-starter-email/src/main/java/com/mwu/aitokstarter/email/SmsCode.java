package com.mwu.aitokstarter.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsCode {

    private String code;
    private Long expireTime;

}
