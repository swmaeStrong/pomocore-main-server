package com.swmStrong.demo.domain.loginCredential.facade;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.common.enums.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public interface LoginCredentialProvider {
    SecurityPrincipal loadPrincipalByToken(UsernamePasswordAuthenticationToken token);
}
