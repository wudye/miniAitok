package com.mwu.aitokservice.ai.service.impl;


import com.mwu.aitiokcoomon.core.compont.SnowFlake;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitok.model.ai.domain.model.ModelAgentDO;
import com.mwu.aitok.model.ai.dto.model.ModelRolePageDTO;
import com.mwu.aitok.model.ai.dto.model.ModelRoleSaveDTO;
import com.mwu.aitok.model.ai.dto.model.web.WebModelRolePageDTO;
import com.mwu.aitok.model.common.enums.StateFlagEnum;
import com.mwu.aitok.model.common.enums.TrueOrFalseEnum;
import com.mwu.aitokservice.ai.controller.admin.AdminModelRoleController;
import com.mwu.aitokservice.ai.mapper.ModelRoleMapper;
import com.mwu.aitokservice.ai.service.IModelRoleService;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AI 智能体表(ModelRole)表服务实现类
 *
 * @author roydon
 * @since 2025-06-02 15:30:43
 */
@Service
public class ModelRoleServiceImpl implements IModelRoleService {
    @Resource
    private ModelRoleMapper modelRoleMapper;

    @Resource
    private SnowFlake snowFlake;

    @Override
    public Long createModelRole(ModelRoleSaveDTO dto) {
        // todo 校验模型是否可用
        ModelAgentDO modelRole = BeanCopyUtils.copyBean(dto, ModelAgentDO.class);
        modelRole.setId(snowFlake.nextId());
        modelRole.setPublicFlag(TrueOrFalseEnum.TRUE.getCode());
        modelRoleMapper.save(modelRole);
        return modelRole.getId();
    }

    @Override
    public void updateModelRole(ModelRoleSaveDTO dto) {
        // todo 校验模型是否可用
        ModelAgentDO modelAgentDO = BeanCopyUtils.copyBean(dto, ModelAgentDO.class);
        modelRoleMapper.save(modelAgentDO);
    }

    @Override
    public void deleteModelRole(Long id) {
        modelRoleMapper.deleteById(id);
    }

    @Override
    public ModelAgentDO getModelRole(Long id) {
        Optional<ModelAgentDO> optional = modelRoleMapper.findById(id);
        return optional.orElse(null);

    }

    @Override
    public PageData<ModelAgentDO> getModelRolePage(ModelRolePageDTO pageDTO) {

        Specification specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(pageDTO.getName())) {

                predicates.add(
                        criteriaBuilder.like(root.get("name"), "%" + pageDTO.getName() + "%")
                );


            }

            if (StringUtils.isNotBlank(pageDTO.getCategory())) {
                predicates.add(
                        criteriaBuilder.like(root.get("categoryIds"), "%" + pageDTO.getCategory() + "%")
                );
            }
            if (StringUtils.isNotBlank(pageDTO.getPublicFlag())) {
                predicates.add(
                        criteriaBuilder.equal(root.get("publicFlag"), pageDTO.getPublicFlag())
                );
            }
            if (StringUtils.isNotBlank(pageDTO.getStateFlag())) {
                predicates.add(
                        criteriaBuilder.equal(root.get("stateFlag"), pageDTO.getStateFlag())
                );
            }

            assert query != null;
            query.orderBy(criteriaBuilder.asc(root.get("sort")));

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageDTO.getPageNum() - 1, pageDTO.getPageSize());
        org.springframework.data.domain.Page<ModelAgentDO> page = modelRoleMapper.findAll(specification, pageable);
        return PageData.page(page);
    }

    @Override
    public void updateModelRoleState(AdminModelRoleController.ModelRoleStateDTO dto) {
        ModelAgentDO modelAgentDO = BeanCopyUtils.copyBean(dto, ModelAgentDO.class);
        modelAgentDO.setId(dto.id());
        modelRoleMapper.save(modelAgentDO);
    }

    @Override
    public PageData<ModelAgentDO> getModelRolePageForWeb(WebModelRolePageDTO pageDTO) {


        Specification<ModelAgentDO> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(pageDTO.getName())) {

                predicates.add(
                        criteriaBuilder.like(root.get("name"), "%" + pageDTO.getName() + "%")
                );

            }
            if (StringUtils.isNotBlank(pageDTO.getCategory())) {
                predicates.add(
                        criteriaBuilder.like(root.get("categoryIds"), "%" + pageDTO.getCategory() + "%")
                );
            }

            predicates.add(
                    criteriaBuilder.equal(root.get("publicFlag"), TrueOrFalseEnum.TRUE.getCode())
            );
            predicates.add(
                    criteriaBuilder.equal(root.get("stateFlag"), StateFlagEnum.ENABLE.getCode())
            );

            assert query != null;
            query.orderBy(criteriaBuilder.asc(root.get("sort")));

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageDTO.getPageNum() - 1, pageDTO.getPageSize());
        org.springframework.data.domain.Page<ModelAgentDO> page = modelRoleMapper.findAll(specification, pageable);
        return PageData.page(page);

    }
}
