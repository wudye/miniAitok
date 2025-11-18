package com.mwu.aitok.model.ai;


import com.mwu.aitok.model.common.BaseDO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;

/**
 * AI管理员表(AiManager)实体类
 *
 * @author mwu
 * @since 2025-05-30 23:39:16
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "ai_manager")
public abstract class AiManagerDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = -17259521472001941L;
    /**
     * 管理员编号
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 备注
     */
    private String remark;


}

