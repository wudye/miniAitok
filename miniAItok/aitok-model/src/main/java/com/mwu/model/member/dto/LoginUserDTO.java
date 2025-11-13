package com.mwu.model.member.dto;

import lombok.Data;
/**
 * login in user Data Transfer Object
 *
 * @author mwu
 * @since 2025-11-13
 */
@Data
public class LoginUserDTO {
    /**
     * username
     */
    private String username;

    /**
     * password
     */
    private String password;
}
