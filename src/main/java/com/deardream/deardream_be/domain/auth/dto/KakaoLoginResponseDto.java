package com.deardream.deardream_be.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KakaoLoginResponseDto {
    private String email;
    private String name;
    private String profileImage;
    private boolean isRegistered;
    private boolean isFamilyRegistered;
    private String tempToken;
    private Long kakaoId;
    private String newAccessToken;
    private String newRefreshToken;
}