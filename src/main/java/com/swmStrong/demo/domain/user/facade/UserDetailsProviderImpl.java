package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsProviderImpl implements UserDetailsProvider {

    private final UserRepository userRepository;

    public UserDetailsProviderImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public SecurityPrincipal loadPrincipalByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        return SecurityPrincipal.builder()
                .userId(user.getId())
                .grantedAuthority(new SimpleGrantedAuthority(user.getRole().getAuthority()))
                .build();
    }
}
