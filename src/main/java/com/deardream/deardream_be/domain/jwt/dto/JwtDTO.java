package com.deardream.deardream_be.domain.jwt.dto;

public record JwtDTO (
        String accessToken,
        String refreshToken
) {
}