package com.mwu.aitok.service.video.repository;

import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserVideoCompilationRepository extends JpaRepository<UserVideoCompilation, Long>, JpaSpecificationExecutor<UserVideoCompilation> {
    Long countByCompilationId(Long compilationId);

    Page<UserVideoCompilation> findByCompilationId(Long compilationId);

    Page<UserVideoCompilation> findByCompilationId(Long compilationId, Pageable pageable);
}
