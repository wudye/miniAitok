package com.mwu.model.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * SmsLoginDTO
 * will replace by email login
 *  @author mwu
 *  @date 2025/11/13
 */
@Data
public class SmsLoginDTO {

    @NotBlank
    private String telephone;

    @NotBlank
    private String smsCode;
}
