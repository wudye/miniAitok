package com.mwu.model.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * EmailRegisterDTO
 *
 * @author mwu
 * @since  2025/11/13
 */
public class EmailRegisterDTO {

    @NotBlank
    private String email;

    @NotBlank
    private String code;

    @NotBlank
    @Size(min = 6, max = 18, message = "the length of password must be between 6 and 18 characters")
    private String password;

    @NotBlank
    @Size(min = 6, max = 18, message = "the length of confirmPassword must be between 6 and 18 characters")
    private String confirmPassword;
}
