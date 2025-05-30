package com.swmStrong.demo.config.security.service;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;

public interface SecurityService {
    SecurityPrincipal loadUserByUserId(String userId);
}
