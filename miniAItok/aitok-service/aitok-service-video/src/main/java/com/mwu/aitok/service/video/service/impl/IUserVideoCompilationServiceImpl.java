package com.mwu.aitok.service.video.service.impl;

import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import com.mwu.aitok.model.video.domain.UserVideoCompilationRelation;
import com.mwu.aitok.model.video.domain.VideoCategory;
import com.mwu.aitok.model.video.dto.CompilationVideoPageDTO;
import com.mwu.aitok.model.video.dto.UpdateUserVideoCompilationDTO;
import com.mwu.aitok.model.video.dto.UserVideoCompilationPageDTO;
import com.mwu.aitok.model.video.vo.UserVideoCompilationInfoVO;
import com.mwu.aitok.service.video.repository.UserVideoCompilationRepository;
import com.mwu.aitok.service.video.service.IUserVideoCompilationRelationService;
import com.mwu.aitok.service.video.service.IUserVideoCompilationService;
import com.mwu.aitok.service.video.service.IVideoService;
import com.mwu.aitok.service.video.util.PackageUserVideoCompilationVOProcessor;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IUserVideoCompilationServiceImpl implements IUserVideoCompilationService {

    private final UserVideoCompilationRepository userVideoCompilationRepository;

    /*
    解决循环依赖：如果 PackageUserVideoCompilationVOProcessor 反过来又依赖 UserVideoCompilationService（或其相关 bean），@Lazy 会让 Spring 注入一个延迟初始化的代理，从而打断初始化时的循环依赖。
延迟初始化 / 降低启动开销：被标注的 bean 不会在应用启动阶段立即创建，只有第一次使用时才真正初始化，适合初始化成本高或不常用的组件。
明确使用时机：当 processor 只在部分方法中被调用时，延迟加载可以避免无谓的资源占用。
工作原理简述：Spring 注入的是一个代理（lazy proxy），首次调用代理的方法时才触发目标 bean 的实例化。若没有循环依赖且希望更早创建该 bean，可以移除 @Lazy；若是为了解决循环依赖，保留或考虑通过重构（拆分依赖、使用事件/工厂等）来彻底消除循环关系。
     */
    @Lazy
    private final PackageUserVideoCompilationVOProcessor packageUserVideoCompilationVOProcessor;

    private final IUserVideoCompilationRelationService userVideoCompilationRelationService;

    private final IVideoService videoService;

    /**
     * 分页查询我的合集
     *
     * @param pageDTO
     * @return
     */

    @Override
    public Page<UserVideoCompilation> videoCompilationMyPage(UserVideoCompilationPageDTO pageDTO) {
        if (pageDTO == null || pageDTO.getPageNum() == null || pageDTO.getPageSize() == null) {
            return Page.empty();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            return Page.empty();
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getClaim("userId");
        if (userId == null) {
            return Page.empty();
        }


        Specification<UserVideoCompilation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            if (pageDTO.getTitle() != null && !pageDTO.getTitle().isBlank()) {
                predicates.add(cb.like(root.get("title"), "%" + pageDTO.getTitle() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageable = PageRequest.of(Math.max(0, pageDTO.getPageNum() - 1), pageDTO.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));

        return userVideoCompilationRepository.findAll(spec, pageable);


    }

    @Override
    public Page<UserVideoCompilation> videoCompilationUserPage(UserVideoCompilationPageDTO pageDTO) {
        Long userId = pageDTO.getUserId();
        if (StringUtils.isNull(userId)) {
            return Page.empty();
        }
        Specification<UserVideoCompilation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            if (pageDTO.getTitle() != null && !pageDTO.getTitle().isBlank()) {
                predicates.add(cb.like(root.get("title"), "%" + pageDTO.getTitle() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageable = PageRequest.of(Math.max(0, pageDTO.getPageNum() - 1), pageDTO.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));

        return userVideoCompilationRepository.findAll(spec, pageable);
    }

    //TODO according frontend to decide which one to use
    @Override
    public Long compilationViewCount(Long compilationId) {
        return userVideoCompilationRepository.countByCompilationId(compilationId);
    }
    //TODO according frontend to decide which one to use

    @Override
    public Long compilationLikeCount(Long compilationId) {
        return userVideoCompilationRepository.countByCompilationId(compilationId);

    }

    //TODO according frontend to decide which one to use

    @Override
    public Long compilationFavoriteCount(Long compilationId) {
        return 0L;
    }
    /**
     * 视频数
     *
     * @param compilationId
     */
    @Override
    public Long compilationVideoCount(Long compilationId) {
        return userVideoCompilationRepository.countByCompilationId(compilationId);
    }

    @Override
    public Boolean updateVideoCompilationInfo(UpdateUserVideoCompilationDTO updateUserVideoCompilationDTO) {

        Long compilationId = updateUserVideoCompilationDTO.getCompilationId();
        if (StringUtils.isNull(compilationId)) {
            return false;
        }

        UserVideoCompilation userVideoCompilation = userVideoCompilationRepository.findById(compilationId).orElse(null);
        if (userVideoCompilation == null) {
            return false;
        }
        if (updateUserVideoCompilationDTO.getTitle() == null || updateUserVideoCompilationDTO.getTitle().isBlank()) {
            return false;
        }
        userVideoCompilation.setTitle(updateUserVideoCompilationDTO.getTitle());
        userVideoCompilation.setDescription(updateUserVideoCompilationDTO.getDescription());
        userVideoCompilation.setCoverImage(updateUserVideoCompilationDTO.getCoverImage());
        userVideoCompilationRepository.save(userVideoCompilation);
        return true;

    }

    @Override
    public Page<UserVideoCompilation> compilationVideoPage(CompilationVideoPageDTO pageDTO) {


        Pageable pageable = PageRequest.of(Math.max(0, pageDTO.getPageNum() - 1), pageDTO.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));
        return userVideoCompilationRepository.findByCompilationId(pageDTO.getCompilationId(), pageable);

    }

    @Override
    public UserVideoCompilationInfoVO getCompilationInfoVOByVideoId(String videoId) {
        Long compilationIdByVideoId = userVideoCompilationRelationService.getCompilationIdByVideoId(videoId);
        if (compilationIdByVideoId != null) {
            // 查询合集详情
            UserVideoCompilation byId = userVideoCompilationRepository.findById(compilationIdByVideoId).orElse(null);
            if (byId == null) {
                return null;
            }
            UserVideoCompilationInfoVO userVideoCompilationInfoVO = BeanCopyUtils.copyBean(byId, UserVideoCompilationInfoVO.class);
            userVideoCompilationInfoVO.setPlayCount(1000L);
            userVideoCompilationInfoVO.setVideoCount(20L);
            userVideoCompilationInfoVO.setWeatherFollow(false);
            // 查询视频数量 todo 让前端做
            return userVideoCompilationInfoVO;
        }
        return null;
    }
}
