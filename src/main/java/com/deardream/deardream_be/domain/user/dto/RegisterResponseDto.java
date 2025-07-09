package com.deardream.deardream_be.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponseDto {
    private UserResponseDto user;
    private String accessToken;
    private String refreshToken;
}

