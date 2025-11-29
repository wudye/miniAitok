package com.mwu.aitok.service.video.repository;

import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import com.mwu.aitok.model.video.domain.UserVideoCompilationRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVideoCompilationRepository extends JpaRepository<UserVideoCompilation, Long>, JpaSpecificationExecutor<UserVideoCompilation> {
    Long countByCompilationId(Long compilationId);

    Page<UserVideoCompilation> findAllByCompilationId(Long compilationId, Pageable pageable);




}
