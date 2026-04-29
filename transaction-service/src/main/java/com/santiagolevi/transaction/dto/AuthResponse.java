package com.santiagolevi.transaction.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String accessToken;
    String refreshToken;
    long accessTokenExpiresInSeconds;
    String tokenType;

    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn) {
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .accessTokenExpiresInSeconds(expiresIn)
            .tokenType("Bearer")
            .build();
    }
}
