package com.mwu.aitok.servicememberoauth2.repository;

import com.mwu.aitok.model.member.domain.MemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {
    Optional<MemberInfo> findMemberInfoByUserId(Long userId);
}
