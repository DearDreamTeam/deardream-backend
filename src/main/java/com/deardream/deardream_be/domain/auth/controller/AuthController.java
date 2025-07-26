package com.deardream.deardream_be.domain.auth.controller;

import com.deardream.deardream_be.domain.auth.dto.KakaoLoginResponseDto;
import com.deardream.deardream_be.domain.auth.service.AuthService;
import com.deardream.deardream_be.domain.auth.util.WhiteRedirectUriList;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "인가코드와 redirectUri를 통해 카카오 서버로부터 정보를 내려받습니다.")
    @GetMapping("/login/kakao")
    public ApiResponse<KakaoLoginResponseDto> kakaoLogin(@RequestParam("code") String code, @RequestParam(value = "redirectUri") String redirectUri, @RequestParam(value = "state", required = false) Long familyId) {

        log.info("Received redirectUri={}", redirectUri);

        if (!WhiteRedirectUriList.getAllowedRedirectUris().contains(redirectUri)) {
            boolean allowed = WhiteRedirectUriList.getAllowedRedirectUris().contains(redirectUri);
            log.info("Is redirectUri allowed? {}", allowed);

            throw new GeneralException(ErrorStatus._INVALID_REDIRECT_URI);
        }

        KakaoLoginResponseDto loginResponseDto = authService.loginWithKakao(code, redirectUri, familyId);
        return ApiResponse.onSuccess(loginResponseDto);

    }


    @Operation(summary = "토큰을 재발급 받습니다.")
    @PostMapping("/reissue")
    public ApiResponse<KakaoLoginResponseDto> reissueToken(@RequestHeader("Authorization") String token) {
        try {
            String refreshToken = token.replace("Bearer ", "").trim();
            KakaoLoginResponseDto responseDto = authService.reissueToken(refreshToken);
            return ApiResponse.onSuccess(responseDto);

        } catch (Exception e) {
            log.error("[Reissue Error] 리프레시 토큰 재발급 실패", e);
            throw new GeneralException(ErrorStatus._TOKEN_INVALID);
        }
    }

    @Operation(summary = "일반 로그아웃 : 로그아웃 시 재로그인이 필요하지 않습니다.")
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String token) {
        String accessToken = token.replace("Bearer ", "");
        authService.logout(accessToken);
        return ApiResponse.onSuccess("로그아웃에 성공했습니다.");
    }

    @Operation(summary = "카카오 계정과 함께 로그아웃 : 로그아웃 시 재로그인이 필요합니다.")
    @GetMapping("/logout/kakao-account")
    public ApiResponse<String> logoutKakaoAccount(@RequestHeader(value = "Authorization") String token, HttpServletRequest request) {
        if (token != null && !token.isBlank()) {
            String accessToken = token.replace("Bearer ", "");
            authService.logout(accessToken);
        }
        String logoutRedirectUri = authService.logoutWithKakaoAccount(request);
        return ApiResponse.onSuccess(logoutRedirectUri);
    }

//    @GetMapping("/logout/callback")
//    public ApiResponse<String> kakaoAccountLogoutCallback(@RequestHeader(value = "Authorization", required = false) String token) {
//        if (token != null && !token.isBlank()) {
//            String accessToken = token.replace("Bearer ", "");
//            authService.logout(accessToken);
//        }
//        return ApiResponse.onSuccess("카카오 계정 및 서비스 로그아웃 완료");
//    }


}
