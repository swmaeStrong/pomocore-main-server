package com.swmStrong.demo.config.security.service;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.loginCredential.facade.LoginCredentialProvider;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    private final LoginCredentialProvider loginCredentialProvider;

    public SecurityServiceImpl(LoginCredentialProvider loginCredentialProvider) {
        this.loginCredentialProvider = loginCredentialProvider;
    }

    @Override
    public SecurityPrincipal loadUserByUserId(String userId) {
        return loginCredentialProvider.loadLoginCredentialByUserId(userId);
    }
}
