package com.swmStrong.demo.config.security.provider;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.loginCredential.facade.LoginCredentialProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final LoginCredentialProvider loginCredentialProvider;

    public CustomAuthenticationProvider(LoginCredentialProvider loginCredentialProvider) {
        this.loginCredentialProvider = loginCredentialProvider;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;

        SecurityPrincipal securityPrincipal = loginCredentialProvider.loadPrincipalByToken(token);

        return new UsernamePasswordAuthenticationToken(securityPrincipal, null, securityPrincipal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
