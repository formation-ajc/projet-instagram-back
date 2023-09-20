package com.projet.instagram.repository.security;

import java.util.Optional;

import com.projet.instagram.model.User;
import com.projet.instagram.model.security.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserId(Long userId);

    @Modifying
    int deleteByUser(User user);
}
