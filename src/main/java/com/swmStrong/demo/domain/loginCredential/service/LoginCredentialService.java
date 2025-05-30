package com.swmStrong.demo.domain.loginCredential.service;

import com.swmStrong.demo.domain.loginCredential.dto.UpgradeRequestDto;

public interface LoginCredentialService {
    void upgradeToUser(UpgradeRequestDto upgradeRequestDto);
}
