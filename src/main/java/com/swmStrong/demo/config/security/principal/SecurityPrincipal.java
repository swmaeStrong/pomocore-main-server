package com.swmStrong.demo.config.security.principal;

import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Builder
public record SecurityPrincipal(
        String userId,
        GrantedAuthority grantedAuthority
) implements UserDetails {

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(grantedAuthority);
    }

    @Override
    public String getPassword() {
        return "";
    }
}
