package com.mwu.aitokservice.ai.service.impl;


import com.mwu.aitiokcoomon.core.compont.SnowFlake;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitiokcoomon.core.utils.bean.BeanUtils;
import com.mwu.aitiokcoomon.core.utils.spring.SpringUtils;
import com.mwu.aitok.model.ai.domain.model.ToolDO;
import com.mwu.aitok.model.ai.dto.model.AiToolSaveDTO;
import com.mwu.aitok.model.ai.dto.model.ToolPageDTO;
import com.mwu.aitokservice.ai.controller.admin.AdminToolController;
import com.mwu.aitokservice.ai.mapper.ToolMapper;
import com.mwu.aitokservice.ai.service.IToolService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * AI 工具表(Tool)表服务实现类
 *
 * @author roydon
 * @since 2025-06-05 16:02:48
 */
@RequiredArgsConstructor
@Service
public class ToolServiceImpl implements IToolService {
    private final ToolMapper toolMapper;
    private final SnowFlake snowFlake;

    @Override
    public Long createTool(AiToolSaveDTO dto) {
        // 校验名称是否存在
        validateToolNameExists(dto.getName());

        // 插入
        ToolDO tool = BeanUtils.toBean(dto, ToolDO.class);
        tool.setId(snowFlake.nextId());
        toolMapper.save(tool);
        return tool.getId();
    }

    @Override
    public void updateTool(AiToolSaveDTO dto) {
        // 1.2 校验名称是否存在
        validateToolNameExists(dto.getName());

        // 2. 更新
        ToolDO updateObj = BeanUtils.toBean(dto, ToolDO.class);
        toolMapper.save(updateObj);
    }

    private void validateToolNameExists(String name) {
        try {
            SpringUtils.getBean(name);
        } catch (NoSuchBeanDefinitionException e) {
            throw new RuntimeException("工具不存在");
        }
    }

    @Override
    public void deleteTool(Long id) {
        toolMapper.deleteById(id);
    }

    @Override
    public ToolDO getTool(Long id) {

        Optional<ToolDO> toolOpt = toolMapper.findById(id);
        return toolOpt.orElse(null);
    }

    @Override
    public PageData<ToolDO> getToolPage(ToolPageDTO pageDTO) {
        Specification<ToolDO> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(pageDTO.getName())) {
                predicates.add(cb.like(root.get("name"), "%" + pageDTO.getName() + "%"));
            }
            if (StringUtils.hasText(pageDTO.getDescription())) {
                predicates.add(cb.like(root.get("description"), "%" + pageDTO.getDescription() + "%"));
            }
            if (StringUtils.hasText(pageDTO.getStateFlag())) {
                predicates.add(cb.equal(root.get("stateFlag"), pageDTO.getStateFlag()));
            }
            // Add predicates based on pageDTO fields if needed
            return cb.and(predicates.toArray(new Predicate[0]));
        };
         Pageable pageable = PageRequest.of(pageDTO.getPageNum() - 1, pageDTO.getPageSize());
        Page<ToolDO> page = toolMapper.findAll(specification, pageable);

        return PageData.page(page);
    }

    @Override
    public List<ToolDO> getToolListByState(String state) {
        List<ToolDO> list = toolMapper.findByStateFlag(state);
        return list;
    }

    @Override
    public void updateToolState(AdminToolController.ToolStateDTO dto) {
        ToolDO toolDO = BeanCopyUtils.copyBean(dto, ToolDO.class);
        toolMapper.save(toolDO);
    }

    @Override
    public List<ToolDO> getToolList(List<Long> toolIds) {
        List<ToolDO> toolDOS  = toolMapper.findAllById(toolIds);
        return toolDOS;
    }
}
