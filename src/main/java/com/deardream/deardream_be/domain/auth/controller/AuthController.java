package com.deardream.deardream_be.domain.auth.controller;

import com.deardream.deardream_be.domain.auth.dto.KakaoLoginResponseDTO;
import com.deardream.deardream_be.domain.auth.service.AuthService;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login/kakao")
    public ApiResponse<KakaoLoginResponseDTO> kakaoLogin(@RequestParam("code") String code, HttpServletResponse response) {

        // 1. 카카오 로그인 처리 (accessCode → User)
        User user = authService.loginWithKakao(code);

        // 2. JWT 토큰 발급
        String accessToken = authService.generateAccessToken(user);
        String refreshToken = authService.generateRefreshToken(user);

        // 3. 응답 DTO 구성
        KakaoLoginResponseDTO loginResponse = KakaoLoginResponseDTO.builder()
                .email(user.getEmail())
                .name(user.getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return ApiResponse.onSuccess(loginResponse);
    }
}
