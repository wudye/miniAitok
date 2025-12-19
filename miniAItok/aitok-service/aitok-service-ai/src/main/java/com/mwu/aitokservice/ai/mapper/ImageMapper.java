package com.mwu.aitokservice.ai.mapper;


import com.mwu.aitok.model.ai.image.domain.ImageDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AI文生图表(AiImage)表数据库访问层
 *
 * @author roydon
 * @since 2025-05-06 15:48:46
 */
@Repository
public interface ImageMapper extends JpaRepository<ImageDO, Long> {

    Page<ImageDO> findAllByUserId(Long userId, Pageable pageable);
}

