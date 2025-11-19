package com.mwu.aitok.model.member.domain;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户敏感词信息表(UserSensitive)实体类
 *
 * @author mwu
 * @since 2023-10-29 20:36:06
 **/

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_sensitive")
public class UserSensitive implements Serializable {
    private static final long serialVersionUID = 565052820117877580L;
    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 敏感词
     */
    private String sensitives;
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;


}

