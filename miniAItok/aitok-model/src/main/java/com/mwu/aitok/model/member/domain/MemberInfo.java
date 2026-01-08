package com.mwu.aitok.model.member.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 会员详情表(MemberInfo)实体类
 *
 * @author mwu
 * @since 2023-11-12 22:26:25
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "member_info")

public class MemberInfo implements Serializable {
    private static final long serialVersionUID = -18427092522208701L;
    /**
     * id
     */
    @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
            @Column(name = "info_id")
    private Long infoId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 个人页面背景图片
     */
    @Size(max = 2048, message = "背景图地址过长")
    private String backImage;
    /**
     * 个人描述
     */
    @Size(max = 300, message = "简介不可超过300字符")
    private String description;
    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate birthday;
    /**
     * 省
     */
    @Size(max = 20, message = "省份名称过长")
    private String province;
    /**
     *
     */
    @Size(max = 30, message = "城市名过长")
    private String city;
    /**
     * 区
     */
    @Size(max = 30, message = "区名过长")
    private String region;
    /**
     * 邮编
     */
    @Size(max = 6, message = "邮编支持6位字符")
    private String adcode;
    /**
     * 学校
     */
    @Size(max = 64, message = "学校名称过长")
    private String campus;
    /**
     * 喜欢视频向外展示状态：0展示1隐藏
     */
    private String likeShowStatus;

    /**
     * 收藏视频向外展示状态：0展示1隐藏
     */
    private String favoriteShowStatus;

}
