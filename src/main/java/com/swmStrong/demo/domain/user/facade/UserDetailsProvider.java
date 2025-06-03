package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;

public interface UserDetailsProvider {
    SecurityPrincipal loadPrincipalByUserId(String userId);
}
