package com.mwu.aitok.model.ai.domain.knowledge;


import com.mwu.aitok.model.ai.domain.model.ModelAgentDO;
import com.mwu.aitok.model.common.BaseDO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * AI 知识库表(Knowledge)实体类
 *
 * @author mwu
 * @since 2025-11-14
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ai_knowledge")
public class KnowledgeDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = 341998039512368993L;
    /**
     * 编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 知识库名称
     */
    private String name;
    /**
     * 封面图片
     */
    private String coverImg;
    /**
     * 知识库描述
     */
    private String description;
    /**
     * 向量模型编号
     */
    private Long embeddingModelId;
    /**
     * 向量模型标识
     */
    private String embeddingModel;
    /**
     * topK
     */
    private Integer topK;
    /**
     * 相似度阈值
     */
    private BigDecimal similarityThreshold;
    /**
     * 是否启用
     */
    private String stateFlag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="ai_model_agent_id")
    private ModelAgentDO modelAgent;

}

