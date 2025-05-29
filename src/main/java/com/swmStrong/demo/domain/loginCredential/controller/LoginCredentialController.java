package com.swmStrong.demo.domain.loginCredential.controller;

import com.swmStrong.demo.domain.loginCredential.dto.UpgradeRequestDto;
import com.swmStrong.demo.domain.loginCredential.service.LoginCredentialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class LoginCredentialController {

    private final LoginCredentialService loginCredentialService;

    public LoginCredentialController(LoginCredentialService loginCredentialService) {
        this.loginCredentialService = loginCredentialService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> upgradeToMember(@RequestBody UpgradeRequestDto upgradeRequestDto) {
        loginCredentialService.upgradeToUser(upgradeRequestDto);
        return ResponseEntity.ok().build();
    }
}

