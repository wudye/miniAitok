package com.mwu.atiokservice.behave.repository;

import com.mwu.aitok.model.behave.domain.UserVideoBehave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserVideoBehaveRepository extends JpaRepository<UserVideoBehave, Long> {
}
