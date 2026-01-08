package com.mwu.aitok.servicememberoauth2.repository;

import com.mwu.aitok.model.member.domain.UserSensitive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSensitiveRepository extends JpaRepository<UserSensitive, Long> {
}
