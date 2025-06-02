package com.swmStrong.demo.domain.loginCredential.facade;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.global.Role;

public interface LoginCredentialProvider {
    SecurityPrincipal loadPrincipalByEmail(String email);
    boolean isPasswordMatched(String email, String password);
    Role loadRoleByUserId(String userId);
}
