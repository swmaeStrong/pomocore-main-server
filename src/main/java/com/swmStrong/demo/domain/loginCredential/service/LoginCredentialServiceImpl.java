package com.swmStrong.demo.domain.loginCredential.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.loginCredential.dto.SocialLoginRequestDto;
import com.swmStrong.demo.domain.loginCredential.dto.UpgradeRequestDto;
import com.swmStrong.demo.domain.loginCredential.entity.LoginCredential;
import com.swmStrong.demo.domain.loginCredential.repository.LoginCredentialRepository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserUpdateProvider;
import com.swmStrong.demo.util.token.TokenUtil;
import com.swmStrong.demo.util.token.dto.RefreshTokenRequestDto;
import com.swmStrong.demo.util.token.dto.SubjectResponseDto;
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
        if (loginCredentialRepository.existsByEmail(upgradeRequestDto.email())) {
            throw new ApiException(ErrorCode.DUPLICATE_USER_EMAIL);
        }

        User user = userUpdateProvider.getUserByUserId(upgradeRequestDto.userId());

        if (user instanceof LoginCredential) {
            throw new ApiException(ErrorCode.USER_ALREADY_REGISTERED);
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

    @Override
    public TokenResponseDto socialLogin(HttpServletRequest request, SocialLoginRequestDto socialLoginRequestDto) {

        String supabaseToken = socialLoginRequestDto.accessToken();

        if (!tokenUtil.isTokenValid(supabaseToken)) {
            throw new ApiException(ErrorCode._INVALID_TOKEN);
        }

        SubjectResponseDto subjectResponseDto = tokenUtil.loadSubjectByToken(supabaseToken);
        String email = subjectResponseDto.email();
        String supabaseId = subjectResponseDto.supabaseId();

        LoginCredential loginCredential = loginCredentialRepository.findBySocialId(supabaseId)
                .orElseGet(() ->
                        loginCredentialRepository.findByEmail(email)
                                .map(existing -> existing.connectSocialAccount(supabaseId))
                                .orElseGet(() -> LoginCredential.createSocialLoginCredential(email, supabaseId)));

        loginCredentialRepository.save(loginCredential);

        return tokenUtil.getToken(loginCredential.getId(), request.getHeader("User-Agent"), loginCredential.getRole());
    }

    @Transactional
    @Override
    public TokenResponseDto upgradeGuestBySocialLogin(
            HttpServletRequest request,
            String userId,
            SocialLoginRequestDto socialLoginRequestDto
    ) {
        if (!tokenUtil.isTokenValid(socialLoginRequestDto.accessToken())) {
            throw new ApiException(ErrorCode._INVALID_TOKEN);
        }
        User user = userUpdateProvider.getUserByUserId(userId);

        SubjectResponseDto subjectResponseDto = tokenUtil.loadSubjectByToken(socialLoginRequestDto.accessToken());
        String email = subjectResponseDto.email();
        String supabaseId = subjectResponseDto.supabaseId();

        loginCredentialRepository.insertLoginCredentialWhenSocialLogin(
                userId,
                email,
                supabaseId
        );
        userUpdateProvider.updateUserRole(user);

        return tokenUtil.getToken(user.getId(), request.getHeader("User-Agent"), user.getRole());

    }
}
