package com.deardream.deardream_be.domain.auth.controller;

import com.deardream.deardream_be.domain.auth.dto.KakaoLoginResponseDto;
import com.deardream.deardream_be.domain.auth.service.AuthService;
import com.deardream.deardream_be.domain.jwt.JwtUtil;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.deardream.deardream_be.global.apiPayload.exception.OnKakaoLoginValidation;
import com.deardream.deardream_be.global.util.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login/kakao")         // 여기로 들어오는 code가 카카오가 준 인가코드
    public ApiResponse<KakaoLoginResponseDto> kakaoLogin(@RequestParam("code") String code, @RequestParam(value = "state", required = false) Long familyId, HttpServletRequest request) {

        KakaoLoginResponseDto loginResponseDto = authService.loginWithKakao(code, familyId, request);
        return ApiResponse.onSuccess(loginResponseDto);

    }


    @PostMapping("/reissue")
    public void reissueToken(@RequestHeader("Authorization") String refreshTokenHeader) {
//        String refreshToken = refreshTokenHeader.replace("Bearer ", "");

//        // 1. 토큰에서 kakaoId 추출
//        Long kakaoId = Long.valueOf(Jwts.parserBuilder()
//                .setSigningKey(jwtUtil.getSecret().getBytes())
//                .build()
//                .parseClaimsJws(refreshToken)
//                .getBody()
//                .getSubject());
//
//        log.debug("토큰 이메일 추출");
//
//        // 2. Redis에서 refresh token 유효성 확인
//        String storedRefreshToken = redisUtil.getData("refresh:" + kakaoId);
//        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
//            throw new IllegalStateException("유효하지 않은 refresh token입니다.");
//        }
//
//        log.debug("리프레쉬 토큰 유효성 확인");
//
//        // 3. 사용자 조회 -> 없을 시 에러
//        User user = userRepository.findByKakaoId(kakaoId)
//                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));
//
//        log.debug("사용자 조회");
//
//        // 4. 새로운 토큰 발급
//        String newAccessToken = authService.generateAccessToken(user);
//        String newRefreshToken = authService.generateRefreshToken(user);
//
//        log.debug("새 토큰 발급");
//
//        redisUtil.deleteData(refreshToken);
//        redisUtil.setDataExpire("refresh:" + kakaoId, newRefreshToken, REFRESH_EXP_TIME);
//
//        log.debug("데이터 지우고 만료기간 처리");
//
//        KakaoLoginResponseDto newRefreshTokenResponse = KakaoLoginResponseDto.builder()
//                .email(user.getEmail())
//                .name(user.getName())
//                .accessToken(newAccessToken)
//                .refreshToken(newRefreshToken)
//                .build();
//
//        return ApiResponse.onSuccess(newRefreshTokenResponse);
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String token) {
        String accessToken = token.replace("Bearer ", "");
        authService.logout(accessToken);
        return ApiResponse.onSuccess("로그아웃에 성공했습니다.");
    }

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
