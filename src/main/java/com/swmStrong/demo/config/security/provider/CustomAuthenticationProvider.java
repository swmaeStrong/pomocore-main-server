package com.swmStrong.demo.config.security.provider;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.loginCredential.facade.LoginCredentialProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
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

        String email = token.getName();
        String password = (String) token.getCredentials();

        if (!loginCredentialProvider.isPasswordMatched(email, password)) {
            throw new BadCredentialsException("비밀번호가 틀렸습니다.");
        }

        SecurityPrincipal securityPrincipal = loginCredentialProvider.loadLoginCredentialByEmail(email);
        return new UsernamePasswordAuthenticationToken(securityPrincipal, null, securityPrincipal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
