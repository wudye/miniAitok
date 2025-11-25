package com.mwu.aitok.model.behave.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * (UserFavorite)实体类
 *
 * @author lzq
 * @since 2023-11-13 14:56:12
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_favorite")
public class UserFavorite implements Serializable {
    private static final long serialVersionUID = 449453316357090990L;
    /**
     * 收藏夹id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * 用户id
     */
    private Long userId;
    /**
     * 收藏夹名称
     */
    @Size(min = 1, max = 20, message = "标题长度需在1~20范围内")
    private String title;
    /**
     * 收藏夹描述
     */
    @Length(max = 100, message = "收藏夹描述长度不能超过100")
    private String description;
    /**
     * 收藏夹封面
     */
    private String coverImage;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @CreationTimestamp
    private LocalDateTime createTime;
    /**
     * 0:别人可见，1:陌生人不可见
     */
    private String showStatus;
    /**
     * 0存在，1删除
     */
    private String delFlag;



}

