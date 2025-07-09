package com.deardream.deardream_be.domain.auth.controller;

import com.deardream.deardream_be.domain.auth.dto.KakaoLoginResponseDto;
import com.deardream.deardream_be.domain.auth.service.AuthService;
import com.deardream.deardream_be.domain.jwt.JwtUtil;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.apiPayload.exception.OnKakaoLoginValidation;
import com.deardream.deardream_be.global.util.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.jsonwebtoken.Jwts;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final RedisUtil redisUtil;
    private final long REFRESH_EXP_TIME = 1000 * 60 * 60 * 24 * 7L;     // 7일
    private final JwtUtil jwtUtil;  // access/refresh 토큰에서 이메일 추출
    private final UserRepository userRepository;    // 이메일로 User 객체 조회용


    @GetMapping("/login/kakao")         // 여기로 들어오는 code가 카카오가 준 인가코드
    public ApiResponse<KakaoLoginResponseDto> kakaoLogin(
            @Validated(OnKakaoLoginValidation.class) @RequestParam("code") String code, @RequestParam(value = "state", required = false) Long familyId, HttpServletResponse response) {

        // 1. 카카오 로그인 처리 (accessCode → User)
        User user = authService.loginWithKakao(code, familyId);

        // 2. JWT 토큰 발급
        String accessToken = authService.generateAccessToken(user);
        String refreshToken = authService.generateRefreshToken(user);
        log.debug("JWT 재발급");
        // refreshToken redis에 저장
//        redisUtil.setDataExpire("refresh:" + user.getKakaoId(), refreshToken, REFRESH_EXP_TIME);

        // refreshToken redis에 저장
        try {
            redisUtil.setDataExpire("refresh:" + user.getKakaoId(), refreshToken, REFRESH_EXP_TIME);
            log.info("refreshToken Redis 저장 성공");
        } catch (Exception e) {
            log.error("refreshToken Redis 저장 실패", e);
            throw e;
        }


//        //3. 응답 DTO 구성
//        KakaoLoginResponseDto loginResponse = KakaoLoginResponseDto.builder()
//                .email(user.getEmail())
//                .name(user.getName())
//                .profileImage(user.getProfileImage())
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .build();
//        return ApiResponse.onSuccess(loginResponse);
        // 3. 응답 DTO 구성
        try {
            KakaoLoginResponseDto loginResponse = KakaoLoginResponseDto.builder()
                    .email(user.getEmail())
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
            log.info("KakaoLoginResponseDto 생성 성공: {}", loginResponse);

            // 4. ApiResponse 직렬화 및 반환
            ApiResponse<KakaoLoginResponseDto> apiResponse = ApiResponse.onSuccess(loginResponse);
            log.info("ApiResponse 직렬화 성공: {}", apiResponse);
            return apiResponse;
        } catch (Exception e) {
            log.error("KakaoLoginResponseDto 생성 또는 ApiResponse 직렬화 중 예외 발생", e);
            throw e;
        }

    }


    @PostMapping("/reissue")
    public ApiResponse<KakaoLoginResponseDto> reissueToken(@RequestHeader("Authorization") String refreshTokenHeader) {
        String refreshToken = refreshTokenHeader.replace("Bearer ", "");

        // 1. 토큰에서 kakaoId 추출
        Long kakaoId = Long.valueOf(Jwts.parserBuilder()
                .setSigningKey(jwtUtil.getSecret().getBytes())
                .build()
                .parseClaimsJws(refreshToken)
                .getBody()
                .getSubject());

        log.debug("토큰 이메일 추출");

        // 2. Redis에서 refresh token 유효성 확인
        String storedRefreshToken = redisUtil.getData("refresh:" + kakaoId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new IllegalStateException("유효하지 않은 refresh token입니다.");
        }

        log.debug("리프레쉬 토큰 유효성 확인");

        // 3. 사용자 조회 -> 없을 시 에러
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));

        log.debug("사용자 조회");

        // 4. 새로운 토큰 발급
        String newAccessToken = authService.generateAccessToken(user);
        String newRefreshToken = authService.generateRefreshToken(user);

        log.debug("새 토큰 발급");

        redisUtil.deleteData(refreshToken);
        redisUtil.setDataExpire("refresh:" + kakaoId, newRefreshToken, REFRESH_EXP_TIME);

        log.debug("데이터 지우고 만료기간 처리");

        KakaoLoginResponseDto newRefreshTokenResponse = KakaoLoginResponseDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();

        return ApiResponse.onSuccess(newRefreshTokenResponse);
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String accessTokenHeader) {
        String accessToken = accessTokenHeader.replace("Bearer ", "");

        // 1. 토큰에서 kakaoId 추출
        Long kakaoId = Long.valueOf(Jwts.parserBuilder()
                .setSigningKey(jwtUtil.getSecret().getBytes())
                .build()
                .parseClaimsJws(accessToken)
                .getBody()
                .getSubject());

        // 2. Redis에서 refresh token 삭제
        redisUtil.deleteData("refresh:" + kakaoId);

        return ApiResponse.onSuccess("로그아웃 성공");
    }


}
