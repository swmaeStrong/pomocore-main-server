package com.swmStrong.demo.config.security.principal;

import com.swmStrong.demo.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class SecurityPrincipal implements UserDetails {
    private final String email;
    private final String password;
    private final GrantedAuthority grantedAuthority;
    private final User user;

    public SecurityPrincipal(String email, String password, GrantedAuthority grantedAuthority, User user) {
        this.email = email;
        this.password = password;
        this.grantedAuthority = grantedAuthority;
        this.user = user;
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
