package com.mwu.aitokservice.ai.controller.web.knowledge;


import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeDO;
import com.mwu.aitok.model.ai.vo.knowledge.web.KnowledgeSimpleVO;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitok.model.common.enums.StateFlagEnum;
import com.mwu.aitokservice.ai.mapper.KnowledgeMapper;
import com.mwu.aitokservice.ai.service.IKnowledgeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * AI 知识库表(Knowledge)表控制层
 *
 * @author roydon
 * @since 2025-06-03 22:03:24
 */
@RestController
@RequestMapping("v1/knowledge")
public class KnowledgeController {

    @Resource
    private IKnowledgeService knowledgeService;

    @Autowired
    private KnowledgeMapper knowledgeMapper;
    /**
     * 新增知识库
     */
    @GetMapping("/list")
    public R<PageData<KnowledgeDO>> list(PageDTO pageDTO) {
        return R.ok(knowledgeService.knowledgeList(pageDTO));
    }

    /**
     * 新增知识库
     */
    @PostMapping
    public R<Long> create(@RequestBody KnowledgeDO dto) {
        return R.ok(knowledgeService.createKnowledge(dto));
    }

    /**
     * 编辑知识库
     */
    @PutMapping
    public R<Long> edit(@RequestBody KnowledgeDO dto) {
        return R.ok(knowledgeService.editKnowledge(dto));
    }

    /**
     * 新增知识库
     */
    @GetMapping("/simple-list")
    public R<List<KnowledgeSimpleVO>> simpleList() {
        Long userId = UserContext.getUserId();
        String deleteFlag = StateFlagEnum.ENABLE.getCode();

        List<KnowledgeDO> knowledgeDOS =

                knowledgeMapper.findByUserIdAndStateFlag( userId, deleteFlag);
        return R.ok(BeanCopyUtils.copyBeanList(knowledgeDOS, KnowledgeSimpleVO.class));
    }

    @GetMapping("/get")
    public R<Optional<KnowledgeDO>> get(@RequestParam("id") Long id) {
        return R.ok(knowledgeMapper.findById(id));
    }

}

