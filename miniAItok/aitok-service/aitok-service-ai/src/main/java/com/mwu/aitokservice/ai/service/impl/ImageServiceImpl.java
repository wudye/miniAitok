package com.mwu.aitokservice.ai.service.impl;


import com.mwu.aitiokcoomon.core.compont.SnowFlake;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitok.model.ai.image.domain.ImageDO;
import com.mwu.aitok.model.ai.image.dto.ImageGenDTO;
import com.mwu.aitok.model.ai.image.enums.ImagePublicFlagEnum;
import com.mwu.aitok.model.ai.image.enums.ImageRadioEnum;
import com.mwu.aitok.model.ai.image.enums.ImageStatusEnum;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitokservice.ai.mapper.ImageMapper;
import com.mwu.aitokservice.ai.service.IImageService;
import com.mwu.aitokstarter.file.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AI文生图表(AiImage)表服务实现类
 *
 * @author roydon
 * @since 2025-05-06 15:48:47
 */
@RequiredArgsConstructor
@Service
public class ImageServiceImpl  implements IImageService {
    private final ImageMapper imageMapper;
    private final OpenAiImageModel openAiImageModel;
    private final SnowFlake snowFlake;
    private final MinioService minioService;

    @Override
    public Page<ImageDO> getList(PageDTO dto) {

        Pageable pageable = PageRequest.of(dto.getPageNum() - 1, dto.getPageSize());
        Page<ImageDO> page = imageMapper.findAllByUserId(UserContext.getUserId(), pageable);
        return page;
    }

    /**
     * 保存图片生成任务
     */
    @Override
    public ImageDO saveImageTask(ImageGenDTO dto) {
        ImageDO imageDO = new ImageDO();
        imageDO.setId(snowFlake.nextId());
        imageDO.setUserId(UserContext.getUserId());
        imageDO.setPrompt(dto.getMessage());
        imageDO.setPublicFlag(ImagePublicFlagEnum.PRIVATE.getCode());
        imageDO.setStatus(ImageStatusEnum.UNFINISHED.getCode());
        imageDO.setCreateTime(LocalDateTime.now());
        imageDO.setCreateBy(UserContext.getUser().getUserName());
        imageDO.setWidth(Objects.requireNonNull(ImageRadioEnum.getByCode(dto.getRadio()), "请选择正确的比例").getWidth());
        imageDO.setHeight(Objects.requireNonNull(ImageRadioEnum.getByCode(dto.getRadio()), "请选择正确的比例").getHeight());
        imageMapper. save(imageDO);
        return imageDO;
    }

    /**
     * 图片生成回调
     *
     * @return url
     */
    @Override
    public ImageDO generateImageCall(boolean success, ImageDO imageNew, Image image, String errorMessage) {
        String url = "";
        if (!success) {
            // 失败
            imageNew.setStatus(ImageStatusEnum.FAILED.getCode());
            imageNew.setErrorMessage(errorMessage);
        } else {
            // 成功 todo 转储图片失败逻辑？
            //minioService.putImageUrl(image.getUrl(), "ai");
            url ="";
            imageNew.setStatus(ImageStatusEnum.FINISHED.getCode());
            imageNew.setPicUrl(url);
        }
        imageNew.setFinishTime(LocalDateTime.now());
        imageMapper.save(imageNew);
        return imageNew;
    }

    @Override
    public ImageDO generateImage(ImageGenDTO dto) {
        // 1、记录入库
        ImageDO imageNew = saveImageTask(dto);
        // 2、ai生图
        boolean generateImageCall = true;
        Image output = null;
        String errorMessage = null;
        try {
            ImageOptions options = buildImageOptions(dto);
            ImageResponse imageResponse = openAiImageModel.call(new ImagePrompt(dto.getMessage(), options));
            output = imageResponse.getResult().getOutput();
        } catch (Exception ex) {
            generateImageCall = false;
            errorMessage = ex.getMessage();
        }
        ImageDO res = generateImageCall(generateImageCall, imageNew, output, errorMessage);
        if (!generateImageCall) {
            throw new RuntimeException("生成图片失败");
        }
        return res;
    }

    private static ImageOptions buildImageOptions(ImageGenDTO dto) {
        return OpenAiImageOptions.builder()
                .withWidth(Objects.requireNonNull(ImageRadioEnum.getByCode(dto.getRadio()), "请选择正确的比例").getWidth())
                .withHeight(Objects.requireNonNull(ImageRadioEnum.getByCode(dto.getRadio()), "请选择正确的比例").getHeight())
//                    .withStyle(MapUtil.getStr(draw.getOptions(), "style")) // 风格
                .withResponseFormat("b64_json")
                .build();
    }

}
