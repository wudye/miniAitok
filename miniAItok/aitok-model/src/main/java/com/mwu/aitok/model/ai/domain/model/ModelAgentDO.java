package com.mwu.aitok.model.ai.domain.model;


import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeDO;
import com.mwu.aitok.model.common.BaseDO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 智能体表(ModelRole)实体类
 *
 * @author mwu
 * @since 2025-06-02 15:30:42
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ai_model_agent")
public class ModelAgentDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = 197421509258722922L;
    /**
     * 角色编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 模型编号
     */
    private Long modelId;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 角色类别
     */
    //@TableField(typeHandler = LongListTypeHandler.class)
    @OneToMany(mappedBy = "modelAgent", cascade = CascadeType.ALL)
    private List<ModelAgentCategoryDO> categoryIds;
    /**
     * 角色描述
     */
    private String description;
    /**
     * 对话开场白
     */
    private String chatPrologue;
    /**
     * 角色上下文
     */
    private String systemMessage;
    /**
     * 是否公开[0私有1公开]
     */
    private String publicFlag;
    /**
     * 状态
     */
    private String stateFlag;
    /**
     * 角色排序
     */
    private Integer sort;
    /**
     * 引用的知识库编号列表
     * <p>
     * 关联 {@link KnowledgeDO#getId()} 字段
     */
   // @TableField(typeHandler = LongListTypeHandler.class)
    @OneToMany(mappedBy = "modelAgent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<KnowledgeDO> knowledgeIds = new ArrayList<>();
    /**
     * 引用的工具编号列表
     * <p>
     * 关联 {@link ToolDO#getId()} 字段
     */
    //@TableField(typeHandler = LongListTypeHandler.class)
    @OneToMany(mappedBy = "modelAgent", cascade = CascadeType.ALL)
    private List<ToolDO> toolIds = new ArrayList<>();


}

