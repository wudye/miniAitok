package com.mwu.aitok.model.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * LoginUserDTO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/24
 **/
@Getter
@Setter
public class SmsLoginDTO {

    @NotBlank
    private String email;

    @NotBlank
    private String smsCode;
}
