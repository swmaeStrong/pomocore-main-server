package com.swmStrong.demo.domain.loginCredential.service;

import com.swmStrong.demo.domain.loginCredential.dto.UpgradeRequestDto;
import com.swmStrong.demo.domain.loginCredential.entity.LoginCredential;
import com.swmStrong.demo.domain.loginCredential.repository.LoginCredentialRepository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserUpdateProvider;
import com.swmStrong.demo.util.token.TokenUtil;
import com.swmStrong.demo.util.token.dto.RefreshTokenRequestDto;
import com.swmStrong.demo.util.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginCredentialServiceImpl implements LoginCredentialService {

    private final LoginCredentialRepository loginCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserUpdateProvider userUpdateProvider;
    private final TokenUtil tokenUtil;

    public LoginCredentialServiceImpl(
            LoginCredentialRepository loginCredentialRepository,
            PasswordEncoder passwordEncoder,
            UserUpdateProvider userUpdateProvider,
            TokenUtil tokenUtil
            ) {
        this.loginCredentialRepository = loginCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.userUpdateProvider = userUpdateProvider;
        this.tokenUtil = tokenUtil;
    }

    @Transactional
    public void upgradeToUser(UpgradeRequestDto upgradeRequestDto) {
        User user = userUpdateProvider.getUserByUserId(upgradeRequestDto.userId());

        if (user instanceof LoginCredential) {
            throw new IllegalArgumentException("가입된 회원");
        }

        loginCredentialRepository.insertLoginCredential(
                upgradeRequestDto.userId(),
                upgradeRequestDto.email(),
                passwordEncoder.encode(upgradeRequestDto.password())
        );
        userUpdateProvider.updateUserRole(user);
    }

    @Override
    public TokenResponseDto tokenRefresh(String userId, HttpServletRequest request, RefreshTokenRequestDto refreshTokenRequestDto) {
        return tokenUtil.tokenRefresh(userId, refreshTokenRequestDto, request.getHeader("User-Agent"));
    }
}
