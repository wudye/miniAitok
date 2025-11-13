package com.mwu.model.member.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * memeber info table entity
 *
 * @author mwu
 * @since 205-11-13
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
    private Long infoId;
    /**
     * user id
     */
    private Long userId;
    /**
     * background image url
     */
    @Size(max = 255, message = "the length of background image url cannot exceed 255 characters")
    private String backImage;
    /**
     * personal description
     */
    @Size(max = 300, message = "the length of personal description cannot exceed 300 characters")
    private String description;
    /**
     * birthday
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Vienna")
    private LocalDateTime birthday;
    /**
     * country
     */
    @Size(max = 50, message = "the length of country name cannot exceed 50 characters")
    private String country;
    /**
     * province
     */
    @Size(max = 50, message = "the length of province name cannot exceed 50 characters")
    private String province;
    /**
     * city
     */
    @Size(max = 30, message = "city name too long")
    private String city;
    /**
     * region
     */
    @Size(max = 30, message = "region name too long")
    private String region;
    /**
     * postal code
     */
    @Size(max = 10, message = "postcode too long")
    private String adcode;
    /**
     * campus
     */
    @Size(max = 255, message = "campus name too long")
    private String campus;
    /**
     * like video display status: 0 show 1 hide
     */
    private String likeShowStatus;

    /**
     * favorite video display status: 0 show 1 hide
     */
    private String favoriteShowStatus;

}
