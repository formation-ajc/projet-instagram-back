package com.projet.instagram.service.security;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.projet.instagram.exception.TokenRefreshException;
import com.projet.instagram.model.security.RefreshToken;
import com.projet.instagram.repository.UserRepository;
import com.projet.instagram.repository.security.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    @Value("${jwt.refreshTokenDurationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        if(refreshTokenRepository.findByUserId(userId).isPresent()) {
            refreshToken = refreshTokenRepository.findByUserId(userId).get();
        }

        if(userRepository.findById(userId).isPresent()) {
            refreshToken.setUser(userRepository.findById(userId).get());
        }

        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        if(userRepository.findById(userId).isPresent()) {
            return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
        }
        return -1;
    }
}
