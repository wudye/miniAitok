package com.mwu.aitokservice.creator.repository;

import com.mwu.aitok.model.video.domain.Video;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class VideoSpecification {

    public static Specification<Video> countQuery(
            Long userId,
            String videoTitle,
            String publishType,
            String showType,
            String positionFlag,
            String auditsStatus
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("userId"), userId));
            predicates.add(cb.equal(root.get("delFlag"), "0"));

            if (StringUtils.hasText(videoTitle)) {
                predicates.add(cb.like(root.get("videoTitle"), "%" + videoTitle + "%"));
            }
            if (StringUtils.hasText(publishType)) {
                predicates.add(cb.equal(root.get("publishType"), publishType));
            }
            if (StringUtils.hasText(showType)) {
                predicates.add(cb.equal(root.get("showType"), showType));
            }
            if (StringUtils.hasText(positionFlag)) {
                predicates.add(cb.equal(root.get("positionFlag"), positionFlag));
            }
            if (StringUtils.hasText(auditsStatus)) {
                predicates.add(cb.equal(root.get("auditsStatus"), auditsStatus));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
