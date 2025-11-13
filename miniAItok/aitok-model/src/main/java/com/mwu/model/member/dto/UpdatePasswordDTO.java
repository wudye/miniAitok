package com.mwu.model.member.dto;

import lombok.Data;

/**
 * update password dto
 *
 * @author mwu
 * @since  2025/11/13
 */
@Data
public class UpdatePasswordDTO {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
