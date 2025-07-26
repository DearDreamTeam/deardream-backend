package com.deardream.deardream_be.domain.auth.service;

import com.deardream.deardream_be.domain.auth.dto.KakaoDto;
import com.deardream.deardream_be.domain.auth.dto.KakaoLoginResponseDto;
import com.deardream.deardream_be.domain.auth.util.KakaoUtil;
//import com.deardream.deardream_be.domain.jwt.util.JwtUtil;
import com.deardream.deardream_be.domain.jwt.JwtUtil;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.deardream.deardream_be.global.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImplementation implements AuthService {

//    private final RestTemplate restTemplate;
    private final KakaoUtil kakaoUtil;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisUtil redisUtil;
    private final long REFRESH_EXP_TIME = 1000 * 60 * 60 * 24 * 7L;     // 7일


    @Override
    @Transactional
    public KakaoLoginResponseDto loginWithKakao(String code, String redirectUri, Long familyId) {

        // 1. 카카오에서 access token 요청
        KakaoDto.OAuthToken tokenResponse = kakaoUtil.getAccessToken(code, redirectUri);

        // 2. 카카오에서 사용자 정보 요청
        KakaoDto.KakaoProfile kakaoProfile = kakaoUtil.getUserInfo(tokenResponse.getAccess_token());
        Long kakaoId = kakaoProfile.getId();
        String email = kakaoProfile.getKakao_account().getEmail();
        String profileImage = kakaoProfile.getKakao_account().getProfile().getProfile_image_url();
        String nickname = kakaoProfile.getKakao_account().getProfile().getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = kakaoProfile.getProperties().getNickname();
        }



        // db에서 kakaoId 존재 여부 확인 -> 있으면 유저 있는 것
        boolean isRegistered = userRepository.existsByKakaoId(kakaoId);
        boolean isFamilyRegistered = false;
        String newAccessToken = null;
        String newRefreshToken = null;

        if(isRegistered){
            // 가입된 유저가 있으면 엔티티를 불러와 family_id 유무 확인
            User entity = userRepository.findByKakaoId(kakaoId)
                    .orElseThrow(() -> new GeneralException((ErrorStatus._USER_NOT_FOUND)));
            isFamilyRegistered = (entity.getFamily() != null);

            newAccessToken = jwtUtil.createAccessToken(entity.getKakaoId(), entity.getRole(), entity.getId());
            newRefreshToken = jwtUtil.createRefreshToken(entity.getKakaoId(), entity.getRole(), entity.getId());
            redisUtil.setDataExpire("refresh:" + kakaoId, newRefreshToken, REFRESH_EXP_TIME);
        }

        // 임시 토큰 발급(유저 등록 전용)
        String tempToken = jwtUtil.createTempToken(kakaoId);

        // 응답 dto 구성
        return KakaoLoginResponseDto.builder()
                .email(email)
                .name(nickname)
                .profileImage(profileImage)
                .isRegistered(isRegistered)
                .isFamilyRegistered(isFamilyRegistered)
                .tempToken(tempToken)
                .newAccessToken(newAccessToken)
                .newRefreshToken(newRefreshToken)
                .kakaoId(kakaoId)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public KakaoLoginResponseDto reissueToken(String refreshToken) {

        // Bearer prefix 제거
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7).trim();
        } else {
            refreshToken = refreshToken.trim();
        }

        // 1. JWT 파싱 (유효성 검사)
        try {
            jwtUtil.parseClaims(refreshToken);
        } catch (Exception e) {
            log.error("[Reissue] 유효하지 않은 리프레시 토큰: {}", refreshToken, e);
            throw new GeneralException(ErrorStatus._TOKEN_INVALID);
        }

        // 2. JWT type 클레임 체크 (access/refresh 구분)
        String tokenType = jwtUtil.parseClaims(refreshToken).get("type", String.class);
        if (!"refresh".equals(tokenType)) {
            log.error("[Reissue] 잘못된 토큰 타입(type): {}", tokenType);
            throw new GeneralException(ErrorStatus._TOKEN_INVALID);
        }

        // 3. kakaoId 추출 및 Redis key 설정
        Long kakaoId = jwtUtil.getKakaoId(refreshToken);
        String redisKey = "refresh:" + kakaoId;

        // 4. redis에 저장된 리프레시 토큰과 비교
        String storedRefreshToken = redisUtil.getData(redisKey);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            log.error("[Reissue] 저장된 토큰과 불일치 또는 만료 (탈취 위험). kakaoId: {}", kakaoId);
            throw new GeneralException(ErrorStatus._TOKEN_INVALID);
        }

        // 5. 사용자 정보 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 6. 새 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(user.getKakaoId(), user.getRole(), user.getId());
        String newRefreshToken = jwtUtil.createRefreshToken(user.getKakaoId(), user.getRole(), user.getId());

        // 7. Redis 갱신 (기존 리프레시 토큰 삭제 후 새 토큰 저장)
        redisUtil.deleteData(redisKey);
        redisUtil.setDataExpire(redisKey, newRefreshToken, REFRESH_EXP_TIME);

        // 8. DTO 반환
        log.info("[Reissue] 새 토큰 발급 완료 for kakaoId={}", user.getKakaoId());
        return KakaoLoginResponseDto.builder()
                .name(user.getName())
                .isRegistered(true)
                .isFamilyRegistered(user.getFamily() != null)
                .kakaoId(user.getKakaoId())
                .newAccessToken(newAccessToken)
                .newRefreshToken(newRefreshToken)
                .build();
    }


    // 기본 로그아웃 - 토큰만 만료
    public void logout(String accessToken) {

        // 1. jwt 유효성 검사 및 파싱
        Long kakaoId = jwtUtil.getKakaoId(accessToken);

        // 2. refresh 토큰/인증정보 redis에서 삭제
        redisUtil.deleteData("refresh:" + kakaoId);

    }

    // 카카오 계정과 함께 로그아웃 -> 카카오 로그아웃 이후 리다이렉트 uri
    // 클라이언트가 이 url로 리다이렉트하면 카카오 계정 세션까지 종료됨
    public String logoutWithKakaoAccount(HttpServletRequest request) {
        return kakaoUtil.logoutWithKakaoAccount(request);
    }
}
