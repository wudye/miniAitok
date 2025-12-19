package com.mwu.aitokservice.ai.controller.web.image;


import com.mwu.aitiokcoomon.core.compont.SnowFlake;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.ai.image.domain.ImageDO;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitokservice.ai.mapper.ImageMapper;
import com.mwu.aitokservice.ai.service.IImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * AI文生图表(AiImage)表控制层
 *
 * @author roydon
 * @since 2025-05-06 15:48:46
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/image")
public class ImageController {

    private final IImageService imageService;
    private final SnowFlake snowFlake;
    private final ImageMapper imageMapper;

    /**
     * 分页
     */
    @PostMapping("/list")
    public PageData<ImageDO> queryByPage(@Validated @RequestBody PageDTO dto) {
        return PageData.page(imageService.getList(dto));
    }

    /**
     * 查询单条
     */
    @GetMapping("/{id}")
    public R<?> queryById(@PathVariable("id") Long id) {
        return R.ok(imageMapper.findById(id).orElse(null));
    }

    /**
     * 新增
     */
    @PostMapping
    public R<?> add(@RequestBody ImageDO imageDO) {
        imageDO.setId(snowFlake.nextId());
        imageDO.setUserId(UserContext.getUserId());
        imageDO.setCreateBy(UserContext.getUser().getUserName());
        LocalDateTime localDateTime = LocalDateTime.now();
        imageDO.setCreateTime(localDateTime);
        return R.ok(imageMapper.save(imageDO));
    }

    /**
     * 编辑
     */
    @PutMapping
    public R<?> edit(ImageDO imageDO) {
        imageDO.setUpdateBy(UserContext.getUser().getUserName());
        imageDO.setUpdateTime(LocalDateTime.now());
        return R.ok(imageMapper.save(imageDO));
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public R<?> removeById(@PathVariable("id") Long id) {
        imageMapper.deleteById(id);
        return R.ok("删除成功");
    }

}

