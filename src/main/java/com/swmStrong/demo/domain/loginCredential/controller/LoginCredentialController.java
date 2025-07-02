package com.swmStrong.demo.domain.loginCredential.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.loginCredential.dto.LoginRequestDto;
import com.swmStrong.demo.domain.loginCredential.dto.SocialLoginRequestDto;
import com.swmStrong.demo.domain.loginCredential.dto.UpgradeRequestDto;
import com.swmStrong.demo.domain.loginCredential.service.LoginCredentialService;
import com.swmStrong.demo.infra.token.dto.RefreshTokenRequestDto;
import com.swmStrong.demo.infra.token.dto.TokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 및 인가")
@RestController
@RequestMapping("/auth")
public class LoginCredentialController {

    private final LoginCredentialService loginCredentialService;

    public LoginCredentialController(LoginCredentialService loginCredentialService) {
        this.loginCredentialService = loginCredentialService;
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "비회원 -> 회원 전환",
            description =
                "<p> 비회원에서 회원으로 전환한다. </p>" +
                "<p> 당장은 바로 회원 가입하는 기능이 없다. (추가 예정) </p>"
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponseDto>> upgradeToUser(
            HttpServletRequest request,
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal,
            @RequestBody @Valid UpgradeRequestDto upgradeRequestDto
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                loginCredentialService.upgradeToUser(request, securityPrincipal.userId(), upgradeRequestDto)
        );
    }

    @Operation(
            summary = "토큰 리프레시",
            description =
                "<p> 리프레시 토큰을 주고, 리프레시토큰과 액세스 토큰을 재발급한다. </p>" +
                "<p> User-Agent가 다르면 리프레시가 안된다. </p>"
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDto>> tokenRefresh(
            HttpServletRequest request,
            @RequestBody RefreshTokenRequestDto refreshTokenRequestDto
    ) {
        return CustomResponseEntity.of(
                SuccessCode._CREATED,
                loginCredentialService.tokenRefresh(request, refreshTokenRequestDto)
        );
    }

    @Operation(
            summary = "로그인",
            description =
                "<p> 로그인한다. </p>" +
                "<p> 해당 기능은 spring security로 선언해 service 레이어에서 찾을 수 없다. </p>"
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(@RequestBody LoginRequestDto loginRequestDto) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                new TokenResponseDto(null, null)
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "로그아웃",
            description =
                "<p> 로그아웃한다. </p>" +
                "<p> 해당 기능은 spring security로 선언해 service 레이어에서 찾을 수 없다. </p>"
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return CustomResponseEntity.of(SuccessCode._OK);
    }

    @Operation(
            summary = "소셜 로그인",
            description =
                "<p> supabase에서 받아온 토큰을 기반으로 로그인한다. </p>" +
                "<p> 1. supabase에서 받아온 socialId가 이미 연동되어 있으면 바로 로그인한다. </p>" +
                "<p> 2. supabase에서 가져온 이메일이 이미 우리 서버에 등록되어 있으면 그 아이디와 연동한다. </p>" +
                "<p> 3. 신규유저의 경우 새로 아이디를 만들어 준다. //TODO: 이 경우 일반 로그인은 불가능하게 처리한다. </p>"
    )
    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<TokenResponseDto>> socialLogin(HttpServletRequest request, @RequestBody SocialLoginRequestDto socialLoginRequestDto) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                loginCredentialService.socialLogin(request, socialLoginRequestDto)
        );
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "소셜 로그인 (비회원 연동 전용)",
            description =
                "<p> supabase에서 받아온 토큰을 기반으로 로그인한다. </p>" +
                "<p> 비회원 유저의 토큰을 같이 보내 유저와 소셜 로그인 계정을 연동한다. </p>" +
                "<p> //TODO: 이 경우 일반 로그인은 불가능하게 처리한다. </p>"
    )
    @PostMapping("/social-login/guest")
    public ResponseEntity<ApiResponse<TokenResponseDto>> guestSocialLogin(
            HttpServletRequest request,
            @AuthenticationPrincipal SecurityPrincipal securityPrincipal,
            @RequestBody SocialLoginRequestDto socialLoginRequestDto
    ) {
        return CustomResponseEntity.of(
                SuccessCode._OK,
                loginCredentialService.upgradeGuestBySocialLogin(request, securityPrincipal.userId(), socialLoginRequestDto)
        );
    }
}

