package com.mwu.aitok.model.social.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户关注表(UserFollow)实体类
 *
 * @author mwu
 * @since 2023-10-30 15:54:20
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_follow")
public class UserFollow implements Serializable {
    private static final long serialVersionUID = -88140804672872294L;
    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    /**
     * 被关注用户ID
     */
    private Long userFollowId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}

