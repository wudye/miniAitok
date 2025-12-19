package com.mwu.aitokservice.notice.repository;

import com.mwu.aitok.model.notice.domain.Notice;
import com.mwu.aitok.model.video.domain.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Video findByVideoId(String videoId);

    void deleteByNoticeId(Long noticeId);

    Long countByNoticeIdAndReceiveFlag(Long noticeId, String receiveFlag);

    Page<Notice> findAllByNoticeUserIdAndNoticeTypeAndReceiveFlag(Long noticeUserId, String noticeType, String receiveFlag, Pageable pageable);

    Long countByNoticeUserIdAndReceiveFlag(Long noticeUserId, String receiveFlag);

    Page<Notice> findAllByNoticeUserIdAndNoticeType(Long noticeUserId, String noticeType, Pageable pageable);

}
