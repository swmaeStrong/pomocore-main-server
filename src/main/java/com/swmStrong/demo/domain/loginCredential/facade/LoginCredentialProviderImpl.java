package com.swmStrong.demo.domain.loginCredential.facade;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.loginCredential.entity.LoginCredential;
import com.swmStrong.demo.domain.loginCredential.repository.LoginCredentialRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginCredentialProviderImpl implements LoginCredentialProvider {

    private final LoginCredentialRepository loginCredentialRepository;

    public LoginCredentialProviderImpl(LoginCredentialRepository loginCredentialRepository) {
        this.loginCredentialRepository = loginCredentialRepository;
    }

    @Override
    public SecurityPrincipal loadLoginCredentialByUserId(String userId) {
        LoginCredential loginCredential = loginCredentialRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        return SecurityPrincipal.builder()
                .userId(loginCredential.getId())
                .email(loginCredential.getEmail())
                .password(loginCredential.getPassword())
                .grantedAuthority(new SimpleGrantedAuthority(loginCredential.getRole().getAuthority()))
                .build();
    }

    @Override
    public SecurityPrincipal loadLoginCredentialByEmail(String email) {
        LoginCredential loginCredential = loginCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일 입니다."));

        return SecurityPrincipal.builder()
                .userId(loginCredential.getId())
                .email(loginCredential.getEmail())
                .password(loginCredential.getPassword())
                .grantedAuthority(new SimpleGrantedAuthority(loginCredential.getRole().getAuthority()))
                .build();
    }
}
