package com.swmStrong.demo.config.security.provider;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.loginCredential.facade.LoginCredentialProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final LoginCredentialProvider loginCredentialProvider;

    public CustomAuthenticationProvider(PasswordEncoder passwordEncoder, LoginCredentialProvider loginCredentialProvider) {
        this.passwordEncoder = passwordEncoder;
        this.loginCredentialProvider = loginCredentialProvider;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;

        String email = token.getName();
        String password = (String) token.getCredentials();

        SecurityPrincipal securityPrincipal = loginCredentialProvider.loadLoginCredentialByEmail(email);

        if (!passwordEncoder.matches(password, securityPrincipal.getPassword())) {
            throw new BadCredentialsException("비밀번호가 틀렸습니다.");
        }

        return new UsernamePasswordAuthenticationToken(securityPrincipal, null, securityPrincipal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
