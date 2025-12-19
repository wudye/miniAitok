package com.mwu.aitokservice.ai.service;


import com.mwu.aitok.model.ai.image.domain.ImageDO;
import com.mwu.aitok.model.ai.image.dto.ImageGenDTO;
import com.mwu.aitok.model.common.dto.PageDTO;
import org.springframework.ai.image.Image;
import org.springframework.data.domain.Page;

/**
 * AI文生图表(AiImage)表服务接口
 *
 * @author roydon
 * @since 2025-05-06 15:48:47
 */
public interface IImageService{

    Page<ImageDO> getList(PageDTO dto);

    /**
     * 保存图片生成任务
     */
    ImageDO saveImageTask(ImageGenDTO dto);

    /**
     * 图片生成回调
     *
     * @return url
     */
    ImageDO generateImageCall(boolean success, ImageDO imageNew, Image image, String errorMessage);

    ImageDO generateImage(ImageGenDTO dto);
}
