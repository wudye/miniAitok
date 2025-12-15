package com.mwu.aitok.servicememberoauth2.repository;

import com.mwu.aitok.model.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/*
@Repository
public interface MemberRepository  extends JpaRepository<Member, Long> {
    @Query("SELECT m FROM Member m WHERE m.userName = ?1")
    Optional<Member> findByUserName(String userName);

}
     */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long>{
    // 明确 JPQL，避免 Spring Data 去尝试 lookup named query
    Optional<Member> findByUserName(String userName);

    // findById 已由 JpaRepository 提供，但如果想保持方法名也可显式声明
    Optional<Member> findByUserId(Long userId);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUserIdAndUserName(Long userId, String userName);
}