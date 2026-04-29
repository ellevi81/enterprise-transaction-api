package com.santiagolevi.transaction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
            "Y2hhbmdlLW1lLWluLXByb2R1Y3Rpb24tbXVzdC1iZS1hdC1sZWFzdC0zMi1jaGFyYWN0ZXJzLWxvbmc=");
        ReflectionTestUtils.setField(jwtService, "accessTokenTtlMs", 900000L);

        user = User.withUsername("alice@test.com").password("pass").authorities(List.of()).build();
    }

    @Test
    void generateAccessToken_producesNonNullToken() {
        String token = jwtService.generateAccessToken(user);
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_roundTrips() {
        String token = jwtService.generateAccessToken(user);
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@test.com");
    }

    @Test
    void isTokenValid_trueForFreshToken() {
        String token = jwtService.generateAccessToken(user);
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_falseForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "accessTokenTtlMs", -1000L);
        String token = jwtService.generateAccessToken(user);
        assertThat(jwtService.isTokenValid(token, user)).isFalse();
    }

    @Test
    void isTokenValid_falseForWrongUser() {
        String token = jwtService.generateAccessToken(user);
        UserDetails otherUser = User.withUsername("bob@test.com").password("pass").authorities(List.of()).build();
        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }
}
