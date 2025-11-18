package com.mwu.aitok.model.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * 启用 JPA 审计，并提供当前操作用户的 AuditorAware 实现

    */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails) {
                return Optional.ofNullable(((UserDetails) principal).getUsername());
            }
            return Optional.of("unknown" + principal);
/*
            if (principal instanceof String) {
                String p = (String) principal;
                // 有时匿名或 simple principal 为 "anonymousUser"，可按需过滤
                return p.isEmpty() ? Optional.empty() : Optional.of(p);
            }
*/

/*
            if (principal instanceof Jwt) {
                Object sub = ((Jwt) principal).getClaims().get("sub");
                if (sub != null) {
                    return Optional.of(String.valueOf(sub));
                }
            }

            String name = auth.getName();
            return (name == null || name.isEmpty()) ? Optional.empty() : Optional.of(name);

 */
        };
    }
}
