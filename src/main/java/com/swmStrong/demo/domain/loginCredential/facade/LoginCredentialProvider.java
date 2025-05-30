package com.swmStrong.demo.domain.loginCredential.facade;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;

public interface LoginCredentialProvider {
    SecurityPrincipal loadLoginCredentialByUserId(String userId);
    SecurityPrincipal loadLoginCredentialByEmail(String email);
}
