package com.swmStrong.demo.domain.loginCredential.facade;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.loginCredential.entity.LoginCredential;
import com.swmStrong.demo.domain.loginCredential.repository.LoginCredentialRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    public Role loadRoleByUserId(String userId) {
        LoginCredential loginCredential = loginCredentialRepository.findById(userId)
                .orElseThrow(IllegalArgumentException::new);
        return loginCredential.getRole();
    }

    @Override
    public SecurityPrincipal loadPrincipalByToken(UsernamePasswordAuthenticationToken token) {
        String email = token.getName();
        String password = token.getCredentials().toString();

        LoginCredential loginCredential = loginCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("등록되지 않은 이메일 입니다."));

        if (!passwordEncoder.matches(password, loginCredential.getPassword())) {
            throw new BadCredentialsException("비밀번호가 틀렸습니다.");
        }

        return SecurityPrincipal.builder()
                .userId(loginCredential.getId())
                .nickname(loginCredential.getNickname())
                .grantedAuthority(new SimpleGrantedAuthority(loginCredential.getRole().getAuthority()))
                .build();
    }
}
