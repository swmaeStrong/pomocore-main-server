package com.swmStrong.demo.domain.loginCredential.service;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.loginCredential.dto.UpgradeRequestDto;
import com.swmStrong.demo.domain.loginCredential.entity.LoginCredential;
import com.swmStrong.demo.domain.loginCredential.repository.LoginCredentialRepository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserDeleteProvider;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import jakarta.transaction.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginCredentialServiceImpl implements LoginCredentialService {

    private final LoginCredentialRepository loginCredentialRepository;
    private final UserInfoProvider userInfoProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserDeleteProvider userDeleteProvider;

    public LoginCredentialServiceImpl(
            LoginCredentialRepository loginCredentialRepository,
            UserInfoProvider userInfoProvider,
            PasswordEncoder passwordEncoder,
            UserDeleteProvider userDeleteProvider
            ) {
        this.loginCredentialRepository = loginCredentialRepository;
        this.userInfoProvider = userInfoProvider;
        this.passwordEncoder = passwordEncoder;
        this.userDeleteProvider = userDeleteProvider;
    }

    @Override
    public SecurityPrincipal loadUserByUsername(String email) throws UsernameNotFoundException {
        LoginCredential loginCredential = loginCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일 입니다."));

        return new SecurityPrincipal(
                loginCredential.getEmail(),
                loginCredential.getPassword(),
                new SimpleGrantedAuthority(loginCredential.getRole().name()),
                loginCredential
        );
    }

    @Transactional
    public void upgradeToUser(UpgradeRequestDto upgradeRequestDto) {
        User user = userInfoProvider.getUserByUserId(upgradeRequestDto.userId());

        if (user instanceof LoginCredential) {
            throw new IllegalArgumentException("가입된 회원");
        }
        userDeleteProvider.deleteUser(user);
        // 혹시라도 여기서 문제가 생기면, 영속성 컨텍스트 문제일 수 있음.
        // entityManager.detach(user); 를 통해 추가적으로 분리해주는 작업이 필요할 수 있음.

        loginCredentialRepository.save(LoginCredential.builder()
                        .user(user)
                        .email(upgradeRequestDto.email())
                        .password(passwordEncoder.encode(upgradeRequestDto.password()))
                        .build()
        );
    }
}
