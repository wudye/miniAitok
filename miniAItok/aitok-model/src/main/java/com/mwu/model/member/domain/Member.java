package com.mwu.model.member.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Member entity representing a user in the system.
 *
 * @author mwu
 * @since 2025-11-13
 *
 */

@Entity
@Table(name = "member")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
//@EntityListeners(AuditingEntityListener.class)
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    /**
     * User account
     */
    private String userName;
    /**
     * User nickname
     */
    private String nickName;
    /**
     * User email
     */
    private String email;
    /**
     * Phone number
     */
    private String telephone;
    /**
     * User gender (0: female, 1: male, 2: unknown)
     */
    private String sex;
    /**
     * Avatar URL
     */
    private String avatar;
    /**
     * Password
     */
    private String password;
    private String salt;
    /**
     * Account status (0: normal, 1: disabled)
     */
    private String status;
    /**
     * Deletion flag (0: exists, 1: deleted)
     */
    private String delFlag;
    /**
     * Last login IP
     */
    private String loginIp;
    /**
     * Last login location
     */
    private String loginLocation;
    /**
     * Last login time
     */
   // @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Vienna")
    private LocalDateTime loginDate;
    /**
     * Creator
     */
    // @CreatedBy
    private String createBy;
    /**
     * Creation time
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Vienna")
    private LocalDateTime createTime;
    /**
     * Updater
     */
  //  @LastModifiedBy
    private String updateBy;
    /**
     * Update time
     */
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Vienna")
    private LocalDateTime updateTime;
}
