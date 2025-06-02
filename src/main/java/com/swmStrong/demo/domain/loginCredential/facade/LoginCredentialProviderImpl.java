package com.swmStrong.demo.domain.loginCredential.facade;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.global.Role;
import com.swmStrong.demo.domain.loginCredential.entity.LoginCredential;
import com.swmStrong.demo.domain.loginCredential.repository.LoginCredentialRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginCredentialProviderImpl implements LoginCredentialProvider {

    private final LoginCredentialRepository loginCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginCredentialProviderImpl(LoginCredentialRepository loginCredentialRepository, PasswordEncoder passwordEncoder) {
        this.loginCredentialRepository = loginCredentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SecurityPrincipal loadPrincipalByEmail(String email) {
        LoginCredential loginCredential = loginCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("등록되지 않은 이메일 입니다."));

        return SecurityPrincipal.builder()
                .userId(loginCredential.getId())
                .nickname(loginCredential.getNickname())
                .grantedAuthority(new SimpleGrantedAuthority(loginCredential.getRole().getAuthority()))
                .build();
    }

    @Override
    public boolean isPasswordMatched(String email, String password) {
        LoginCredential loginCredential = loginCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("등록되지 않은 이메일입니다."));

        return passwordEncoder.matches(password, loginCredential.getPassword());
    }

    @Override
    public Role loadRoleByUserId(String userId) {
        LoginCredential loginCredential = loginCredentialRepository.findById(userId)
                .orElseThrow(IllegalArgumentException::new);
        return loginCredential.getRole();
    }
}
