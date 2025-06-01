package com.swmStrong.demo.domain.loginCredential.repository;

import com.swmStrong.demo.domain.loginCredential.entity.LoginCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginCredentialRepository extends JpaRepository<LoginCredential, String> {
    Optional<LoginCredential> findByEmail(String email);
}