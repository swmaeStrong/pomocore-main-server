package com.swmStrong.demo.domain.loginCredential.facade;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.global.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public interface LoginCredentialProvider {
    Role loadRoleByUserId(String userId);
    SecurityPrincipal loadPrincipalByToken(UsernamePasswordAuthenticationToken token);
}
