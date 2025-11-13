package com.mwu.model.member.dto;

import lombok.Data;

/**
 * mobile register body
 * replace by email register body
 *
 * @author mwu
 * @since  2025/11/13
 */

@Data
public class SmsRegisterBody{

    /**
     * 用户名
     */
    private String telephone;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户密码
     */
    private String confirmPassword;
}