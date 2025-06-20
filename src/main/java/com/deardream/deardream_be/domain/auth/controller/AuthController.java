package com.deardream.deardream_be.domain.auth.controller;

import com.deardream.deardream_be.domain.auth.dto.KakaoLoginResponseDTO;
import com.deardream.deardream_be.domain.auth.service.AuthService;
import com.deardream.deardream_be.domain.jwt.JwtUtil;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.util.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ApiResponse<KakaoLoginResponseDTO> kakaoLogin(@RequestParam("code") String code, HttpServletResponse response) {

        // 1. 카카오 로그인 처리 (accessCode → User)
        User user = authService.loginWithKakao(code);
//        log.debug("카카오 로그인 처리");
        // 2. JWT 토큰 발급
        String accessToken = authService.generateAccessToken(user);
        String refreshToken = authService.generateRefreshToken(user);
        log.debug("JWT 재발급");
        // refreshToken redis에 저장
        redisUtil.setDataExpire("refresh:" + user.getEmail(), refreshToken, REFRESH_EXP_TIME);

        // 3. 응답 DTO 구성
        KakaoLoginResponseDTO loginResponse = KakaoLoginResponseDTO.builder()
                .email(user.getEmail())
                .name(user.getName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return ApiResponse.onSuccess(loginResponse);
    }


    @PostMapping("/reissue")
    public ApiResponse<KakaoLoginResponseDTO> reissueToken(@RequestHeader("Authorization") String refreshTokenHeader) {
        String refreshToken = refreshTokenHeader.replace("Bearer ", "");

        // 1. 토큰에서 이메일 추출
        String email = Jwts.parserBuilder()
                .setSigningKey(jwtUtil.getSecret().getBytes())
                .build()
                .parseClaimsJws(refreshToken)
                .getBody()
                .getSubject();

        log.debug("토큰 이메일 추출");

        // 2. Redis에서 refresh token 유효성 확인
        String storedRefreshToken = redisUtil.getData("refresh:" + email);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new IllegalStateException("유효하지 않은 refresh token입니다.");
        }

        log.debug("리프레쉬 토큰 유효성 확인");

        // 3. 사용자 조회 -> 없을 시 에러
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));

        log.debug("사용자 조회");

        // 4. 새로운 토큰 발급
        String newAccessToken = authService.generateAccessToken(user);
        String newRefreshToken = authService.generateRefreshToken(user);

        log.debug("새 토큰 발급");

        redisUtil.deleteData(refreshToken);
        redisUtil.setDataExpire("refresh:" + email, newRefreshToken, REFRESH_EXP_TIME);

        log.debug("데이터 지우고 만료기간 처리");

        KakaoLoginResponseDTO newRefreshTokenResponse = KakaoLoginResponseDTO.builder()
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

        // 1. 토큰에서 이메일 추출
        String email = Jwts.parserBuilder()
                .setSigningKey(jwtUtil.getSecret().getBytes())
                .build()
                .parseClaimsJws(accessToken)
                .getBody()
                .getSubject();

        // 2. Redis에서 refresh token 삭제
        redisUtil.deleteData("refresh:" + email);

        return ApiResponse.onSuccess("로그아웃 성공");
    }


}
