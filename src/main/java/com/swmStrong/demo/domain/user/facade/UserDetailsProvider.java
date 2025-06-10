package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.common.enums.Role;

public interface UserDetailsProvider {
    SecurityPrincipal loadPrincipalByUserId(String userId);
    Role loadRoleByUserId(String userId);
}
