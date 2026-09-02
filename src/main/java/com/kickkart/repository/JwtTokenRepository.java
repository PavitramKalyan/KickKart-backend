package com.kickkart.repository;

import com.kickkart.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    Optional<JwtToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteByUserId(Long userId);
}
