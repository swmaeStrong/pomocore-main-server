package com.swmStrong.demo.config.security.principal;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Builder
public class SecurityPrincipal implements UserDetails {
    private final String userId;
    private final String email;
    private final String password;
    private final GrantedAuthority grantedAuthority;

    public SecurityPrincipal(String userId, String email, String password, GrantedAuthority grantedAuthority) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.grantedAuthority = grantedAuthority;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(grantedAuthority);
    }

    @Override
    public String getPassword() {
        return password;
    }
}
