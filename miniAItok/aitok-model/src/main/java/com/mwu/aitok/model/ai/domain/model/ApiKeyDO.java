package com.mwu.aitok.model.ai.domain.model;


import com.mwu.aitok.model.common.BaseDO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;

/**
 * AI API 密钥表(AiApiKey)实体类
 *
 * @author mwu
 * @since 2025-05-31 23:44:52
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="ai_api_key")
public class ApiKeyDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = 637950607395494840L;
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
     * 密钥
     */
    private String apiKey;
    /**
     * 平台
     */
    private String platform;
    /**
     * 自定义 API 地址
     */
    private String url;
    /**
     * 状态[0正常1禁用]
     */
    private String stateFlag;


}

