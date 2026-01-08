package com.mwu.aitok.model.ai.image.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mwu.aitok.model.common.BaseDO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * AI文生图表(AiImage)实体类
 *
 * @author mwu
 * @since 2025-05-06 15:48:47
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ai_image")
public class ImageDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = -70265461945174553L;
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
     * 提示词
     */
    private String prompt;
    /**
     * 平台
     */
    private String platform;
    /**
     * 模型
     */
    private String model;
    /**
     * 图片宽度
     */
    private Integer width;
    /**
     * 图片高度
     */
    private Integer height;
    /**
     * 绘画状态
     * {@link com.aitok.model.ai.image.enums.ImageStatusEnum.code}
     */
    private String status;
    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime finishTime;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 是否发布
     */
    private String publicFlag;
    /**
     * 图片地址
     */
    private String picUrl;
    /**
     * 绘制参数
     */
    private String options;
    /**
     * 任务编号
     */
    private String taskId;
    /**
     * mj buttons 按钮
     */
    private String buttons;


}

