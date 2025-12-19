package com.mwu.aitok.model.ai.domain.model;


import com.mwu.aitok.model.common.BaseDO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;

/**
 * AI 工具表(Tool)实体类
 *
 * @author mwu
 * @since 2025-06-05 16:02:47
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ai_tool")
public class ToolDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = -28051785807774847L;
    /**
     * 工具编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 工具名称
     */
    private String name;
    /**
     * 工具描述
     */
    private String description;
    /**
     * 状态
     */
    private String stateFlag;

    @ManyToOne
    @JoinColumn(name ="ai_model_agent_id")
    private ModelAgentDO modelAgent;
}

