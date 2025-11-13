package com.mwu.model.member.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * user_sensitive entity class
 *
 * @author mwu
 * @since 2020-11-13
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "user_sensitive")
public class UserSensitive implements Serializable {
    private static final long serialVersionUID = 565052820117877580L;
    /**
     * primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * sensitive words
     */
    private String sensitives;
    /**
     * created time
     */
    @CreationTimestamp
    private LocalDateTime createdTime;


}