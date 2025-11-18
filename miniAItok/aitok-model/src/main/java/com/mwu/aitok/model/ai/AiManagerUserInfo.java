package com.mwu.aitok.model.ai;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

/**
 * AiManagerUserInfo
 *
 * @AUTHOR: mwu
 * @DATE: 2025/5/31
 **/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ai_manager_user_info")
public class AiManagerUserInfo extends AiManagerDO {
    /**
     * 用户账号
     */
    private String userName;
    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 头像地址
     */
    private String avatar;
}
