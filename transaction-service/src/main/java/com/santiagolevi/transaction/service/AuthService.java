package com.santiagolevi.transaction.service;

import com.santiagolevi.transaction.dto.AuthRequest;
import com.santiagolevi.transaction.dto.AuthResponse;
import com.santiagolevi.transaction.dto.RefreshTokenRequest;
import com.santiagolevi.transaction.model.AppUser;
import com.santiagolevi.transaction.model.RefreshToken;
import com.santiagolevi.transaction.repository.AppUserRepository;
import com.santiagolevi.transaction.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refresh-token-ttl-ms:604800000}")
    private long refreshTokenTtlMs; // 7 days default

    @Transactional
    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        AppUser user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Revoke all existing refresh tokens for this user (single-device policy).
        refreshTokenRepository.revokeAllByUserId(user.getId());

        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = generateRefreshToken(user);

        log.info("User '{}' logged in successfully.", user.getUsername());
        return AuthResponse.of(accessToken, rawRefreshToken, jwtService.getAccessTokenTtlSeconds());
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(() -> new IllegalArgumentException("Refresh token not found."));

        if (!stored.isValid()) {
            throw new IllegalArgumentException("Refresh token is expired or revoked.");
        }

        // Rotate: revoke old token, issue a new pair.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        AppUser user = stored.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        log.info("Refresh token rotated for user '{}'.", user.getUsername());
        return AuthResponse.of(newAccessToken, newRefreshToken, jwtService.getAccessTokenTtlSeconds());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByToken(rawRefreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private String generateRefreshToken(AppUser user) {
        String raw = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.builder()
            .token(raw)
            .user(user)
            .expiresAt(Instant.now().plusMillis(refreshTokenTtlMs))
            .build();
        refreshTokenRepository.save(token);
        return raw;
    }
}
