package com.swmStrong.demo.domain.loginCredential.controller;

import com.swmStrong.demo.domain.loginCredential.dto.LoginRequestDto;
import com.swmStrong.demo.domain.loginCredential.dto.UpgradeRequestDto;
import com.swmStrong.demo.domain.loginCredential.service.LoginCredentialService;
import com.swmStrong.demo.util.token.dto.TokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 및 인가")
@RestController
@RequestMapping("/auth")
public class LoginCredentialController {

    private final LoginCredentialService loginCredentialService;

    public LoginCredentialController(LoginCredentialService loginCredentialService) {
        this.loginCredentialService = loginCredentialService;
    }

    @Operation(
            summary = "비회원 -> 회원 전환",
            description =
                "<p> 비회원에서 회원으로 전환한다. </p>" +
                "<p> 당장은 바로 회원 가입하는 기능이 없다. (추가 예정) </p>"
    )
    @PostMapping("/register")
    public ResponseEntity<Void> upgradeToMember(@RequestBody UpgradeRequestDto upgradeRequestDto) {
        loginCredentialService.upgradeToUser(upgradeRequestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "로그인",
            description =
                "<p> 로그인한다. </p>" +
                "<p> 해당 기능은 spring security로 선언해 service 레이어에서 찾을 수 없다. </p>"
    )
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "로그아웃",
            description =
                "<p> 로그아웃한다. </p>" +
                "<p> 해당 기능은 spring security로 선언해 service 레이어에서 찾을 수 없다. </p>"
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}

