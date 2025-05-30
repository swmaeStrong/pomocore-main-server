package com.swmStrong.demo.domain.loginCredential.facade;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;

public interface LoginCredentialProvider {
    SecurityPrincipal loadPrincipalByUserId(String userId);
    SecurityPrincipal loadPrincipalByEmail(String email);
    boolean isPasswordMatched(String email, String password);
}
