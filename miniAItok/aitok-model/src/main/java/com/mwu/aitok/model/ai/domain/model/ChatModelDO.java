package com.mwu.aitok.model.ai.domain.model;


import com.mwu.aitok.model.common.BaseDO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * AI 聊天模型表(AiChatModel)实体类
 *
 * @author mwu
 * @since 2025-06-02 13:41:27
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ai_chat_model")
public class ChatModelDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = 384820146835711839L;
    /**
     * 编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * API 秘钥编号
     */
    private Long keyId;
    /**
     * 模型名字
     */
    private String name;
    /**
     * 模型标识
     */
    private String model;
    /**
     * 模型平台
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;
    /**
     * 模型类型
     * 枚举 {@link AiModelTypeEnum}
     */
    private String type;
    /**
     * 状态[0正常1禁用]
     */
    private String stateFlag;
    /**
     * 排序
     */
    private Integer sort;

    // ========== 对话配置 ==========

    /**
     * 温度参数
     */
    private BigDecimal temperature;
    /**
     * 单条回复的最大 Token 数量
     */
    private Integer maxTokens;
    /**
     * 上下文的最大 Message 数量
     */
    private Integer maxContexts;


}

