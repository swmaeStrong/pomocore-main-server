package com.swmStrong.demo.domain.loginCredential.repository;

import com.swmStrong.demo.domain.loginCredential.entity.LoginCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LoginCredentialRepository extends JpaRepository<LoginCredential, String> {
    Optional<LoginCredential> findByEmail(String email);
    boolean existsByEmail(String email);
    @Modifying
    @Query(value = "INSERT INTO login_credential (id, email, password) VALUES (:id, :email, :password)", nativeQuery = true)
    void insertLoginCredential(@Param("id") String id, @Param("email") String email, @Param("password") String password);

}