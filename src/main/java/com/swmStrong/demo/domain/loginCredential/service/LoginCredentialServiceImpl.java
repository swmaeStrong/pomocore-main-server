package com.swmStrong.demo.domain.loginCredential.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.loginCredential.dto.SocialLoginRequestDto;
import com.swmStrong.demo.domain.loginCredential.dto.UpgradeRequestDto;
import com.swmStrong.demo.domain.loginCredential.entity.LoginCredential;
import com.swmStrong.demo.domain.loginCredential.repository.LoginCredentialRepository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserUpdateProvider;
import com.swmStrong.demo.infra.token.TokenManager;
import com.swmStrong.demo.infra.token.dto.RefreshTokenRequestDto;
import com.swmStrong.demo.infra.token.dto.SubjectResponseDto;
import com.swmStrong.demo.infra.token.dto.TokenResponseDto;
import com.swmStrong.demo.infra.mail.MailSender;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginCredentialServiceImpl implements LoginCredentialService {

    private final LoginCredentialRepository loginCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserUpdateProvider userUpdateProvider;
    private final TokenManager tokenManager;
    private final MailSender mailSender;

    public LoginCredentialServiceImpl(
            LoginCredentialRepository loginCredentialRepository,
            PasswordEncoder passwordEncoder,
            UserUpdateProvider userUpdateProvider,
            TokenManager tokenManager,
            MailSender mailSender
            ) {
        this.loginCredentialRepository = loginCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.userUpdateProvider = userUpdateProvider;
        this.tokenManager = tokenManager;
        this.mailSender = mailSender;
    }

    @Transactional
    public TokenResponseDto upgradeToUser(
            HttpServletRequest request,
            String userId,
            UpgradeRequestDto upgradeRequestDto
    ) {
        if (loginCredentialRepository.existsByEmail(upgradeRequestDto.email())) {
            throw new ApiException(ErrorCode.DUPLICATE_USER_EMAIL);
        }

        User user = userUpdateProvider.getUserByUserId(userId);

        if (user instanceof LoginCredential) {
            throw new ApiException(ErrorCode.USER_ALREADY_REGISTERED);
        }

        loginCredentialRepository.insertLoginCredential(
                userId,
                upgradeRequestDto.email(),
                passwordEncoder.encode(upgradeRequestDto.password())
        );

        user = userUpdateProvider.updateUserRole(user);

        mailSender.sendWelcomeEmail(upgradeRequestDto.email(), "사용자");
        return tokenManager.getToken(user.getId(), request.getHeader("User-Agent"), user.getRole());
    }

    @Override
    public TokenResponseDto tokenRefresh(HttpServletRequest request, RefreshTokenRequestDto refreshTokenRequestDto) {
        return tokenManager.tokenRefresh(refreshTokenRequestDto, request.getHeader("User-Agent"));
    }

    @Override
    public TokenResponseDto socialLogin(HttpServletRequest request, SocialLoginRequestDto socialLoginRequestDto) {

        String supabaseToken = socialLoginRequestDto.accessToken();

        if (!tokenManager.isTokenValid(supabaseToken)) {
            throw new ApiException(ErrorCode._INVALID_TOKEN);
        }

        SubjectResponseDto subjectResponseDto = tokenManager.loadSubjectByToken(supabaseToken);
        String email = subjectResponseDto.email();
        String supabaseId = subjectResponseDto.supabaseId();

        LoginCredential loginCredential = loginCredentialRepository.findBySocialId(supabaseId)
                .orElseGet(() -> loginCredentialRepository.findByEmail(email)
                        .map(existing -> existing.connectSocialAccount(supabaseId))
                        .orElseGet(() -> createSocialLoginCredential(supabaseId, email)));

        loginCredentialRepository.save(loginCredential);

        return tokenManager.getToken(loginCredential.getId(), request.getHeader("User-Agent"), loginCredential.getRole());
    }

    @Transactional
    @Override
    public TokenResponseDto upgradeGuestBySocialLogin(
            HttpServletRequest request,
            String userId,
            SocialLoginRequestDto socialLoginRequestDto
    ) {
        if (!tokenManager.isTokenValid(socialLoginRequestDto.accessToken())) {
            throw new ApiException(ErrorCode._INVALID_TOKEN);
        }

        User user = userUpdateProvider.getUserByUserId(userId);

        if (user instanceof LoginCredential) {
            throw new ApiException(ErrorCode.USER_ALREADY_REGISTERED);
        }

        SubjectResponseDto subjectResponseDto = tokenManager.loadSubjectByToken(socialLoginRequestDto.accessToken());
        String email = subjectResponseDto.email();
        String supabaseId = subjectResponseDto.supabaseId();

        loginCredentialRepository.insertLoginCredentialWhenSocialLogin(
                userId,
                email,
                supabaseId
        );
        user = userUpdateProvider.updateUserRole(user);

        mailSender.sendWelcomeEmail(email, "사용자");

        return tokenManager.getToken(user.getId(), request.getHeader("User-Agent"), user.getRole());
    }

    private LoginCredential createSocialLoginCredential(
            String email,
            String supabaseId
    ) {
        mailSender.sendWelcomeEmail(email, "사용자");
        return LoginCredential.createSocialLoginCredential(email, supabaseId);
    }
}
