package com.mwu.aitok.model.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * LoginUserDTO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/24
 **/
@Data
public class SmsLoginDTO {

    @NotBlank
    private String telephone;

    @NotBlank
    private String smsCode;
}
