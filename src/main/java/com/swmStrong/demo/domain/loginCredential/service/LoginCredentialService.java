package com.swmStrong.demo.domain.loginCredential.service;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.loginCredential.dto.UpgradeRequestDto;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface LoginCredentialService {
    SecurityPrincipal loadUserByUsername(String email) throws UsernameNotFoundException;
    void upgradeToUser(UpgradeRequestDto upgradeRequestDto);
}
