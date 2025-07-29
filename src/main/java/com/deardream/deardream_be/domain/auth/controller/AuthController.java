package com.deardream.deardream_be.domain.auth.controller;

import com.deardream.deardream_be.domain.auth.dto.KakaoLoginResponseDto;
import com.deardream.deardream_be.domain.auth.service.AuthService;
import com.deardream.deardream_be.domain.auth.util.WhiteRedirectUriList;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.deardream.deardream_be.domain.cookie.CookieService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
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
    private final CookieService cookieService;

    @Operation(summary = "인가코드와 redirectUri를 통해 카카오 서버로부터 정보를 내려받습니다.")
    @GetMapping("/login/kakao")
    public ApiResponse<KakaoLoginResponseDto> kakaoLogin(
            @RequestParam("code") String code,
            @RequestParam(value = "redirectUri") String redirectUri,
            Long familyId,
            HttpServletResponse response) {

        if (!WhiteRedirectUriList.getAllowedRedirectUris().contains(redirectUri)) {
            boolean allowed = WhiteRedirectUriList.getAllowedRedirectUris().contains(redirectUri);
            log.info("Is redirectUri allowed? {}", allowed);
            throw new GeneralException(ErrorStatus._INVALID_REDIRECT_URI);
        }

        KakaoLoginResponseDto loginResponseDto = authService.loginWithKakao(code, redirectUri, familyId);

        // 쿠키 세팅 로직
        // tempToken은 가입 전용, 가입된 유저가 아니면 임시 토큰만 세팅
        if (!loginResponseDto.isRegistered()) {
            if (loginResponseDto.getTempToken() != null) {
                cookieService.setTempTokenCookie(response, loginResponseDto.getTempToken());
            }
        } else { // 가입된 유저면 accessToken, refreshToken 쿠키 세팅
            cookieService.setTokenCookies(response, loginResponseDto.getNewAccessToken(), loginResponseDto.getNewRefreshToken());
        }

        return ApiResponse.onSuccess(loginResponseDto);

    }


    @Operation(summary = "토큰을 재발급 받습니다.")
    @PostMapping("/reissue")
    public ApiResponse<KakaoLoginResponseDto> reissueToken(
            @RequestHeader("Authorization") String token,
            HttpServletResponse response
    ) {
        try {
            String refreshToken = token.replace("Bearer ", "").trim();
            KakaoLoginResponseDto responseDto = authService.reissueToken(refreshToken);

            // 리프레시 후에는 accessToken/refreshToken 쿠키 모두 세팅
            cookieService.setTokenCookies(response, responseDto.getNewAccessToken(), responseDto.getNewRefreshToken());

            return ApiResponse.onSuccess(responseDto);

        } catch (Exception e) {
            log.error("[Reissue Error] 리프레시 토큰 재발급 실패", e);
            throw new GeneralException(ErrorStatus._TOKEN_INVALID);
        }
    }

    @Operation(summary = "일반 로그아웃 : 로그아웃 시 재로그인이 필요하지 않습니다.")
    @PostMapping("/logout")
    public ApiResponse<String> logout(
            @RequestHeader("Authorization") String token,
            HttpServletResponse response
    ) {
        String accessToken = token.replace("Bearer ", "");
        authService.logout(accessToken);

        // accessToken, refreshToken, tempToken 쿠키 모두 삭제
        cookieService.deleteTokenCookies(response);
        cookieService.deleteTempTokenCookie(response);

        return ApiResponse.onSuccess("로그아웃에 성공했습니다.");
    }

    @Operation(summary = "카카오 계정과 함께 로그아웃 : 로그아웃 시 재로그인이 필요합니다.")
    @GetMapping("/logout/kakao-account")
    public ApiResponse<String> logoutKakaoAccount(
            @RequestHeader(value = "Authorization") String token,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (token != null && !token.isBlank()) {
            String accessToken = token.replace("Bearer ", "");
            authService.logout(accessToken);

            // accessToken, refreshToken, tempToken 쿠키 모두 삭제
            cookieService.deleteTokenCookies(response);
            cookieService.deleteTempTokenCookie(response);
        }
        String logoutRedirectUri = authService.logoutWithKakaoAccount(request);
        return ApiResponse.onSuccess(logoutRedirectUri);
    }


}
