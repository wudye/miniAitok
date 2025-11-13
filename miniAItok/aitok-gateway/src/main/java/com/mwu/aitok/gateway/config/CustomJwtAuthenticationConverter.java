package com.mwu.aitok.gateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);
        Object userId = jwt.getClaims().getOrDefault("username", jwt.getSubject());
        Map<String, Object> details = new HashMap<>();
        details.put("username", userId);

        System.out.println("username" +  userId);
        token.setDetails(details);
        return token;
    }
}
