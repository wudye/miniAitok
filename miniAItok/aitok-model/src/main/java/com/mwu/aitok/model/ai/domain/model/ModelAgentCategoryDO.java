package com.mwu.aitok.model.ai.domain.model;


import com.mwu.aitok.model.common.BaseDO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;

/**
 * AI 智能体分类表(AiModelAgentCategory)实体类
 *
 * @author mwu
 * @since 2025-06-13 10:21:49
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ai_model_agent_category")
public class ModelAgentCategoryDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = -26433566383406754L;
    /**
     * 编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 图标
     */
    private String icon;
    /**
     * 角色排序
     */
    private Integer sort;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "model_agent_id")
    private ModelAgentDO modelAgent;
}

