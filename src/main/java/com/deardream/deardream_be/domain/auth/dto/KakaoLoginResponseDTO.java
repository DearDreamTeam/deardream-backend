package com.deardream.deardream_be.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class KakaoLoginResponseDTO {
    private String email;
    private String name;
    private String accessToken;
    private String refreshToken;
}