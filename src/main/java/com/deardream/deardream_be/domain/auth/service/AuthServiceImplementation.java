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
    public KakaoLoginResponseDto loginWithKakao(String code, Long familyId, HttpServletRequest request) {

        // host 정보로 redirectUri 결정
        String host = request.getHeader("host");
        String redirectUri = switch (host) {
            case "localhost:3000" -> "http://localhost:3000";
            case "localhost:8080" -> "http://localhost:8080";
            case "deardream.site", "www.deardream.site" -> "https://www.deardream.site";
            default -> "https://www.deardream.site";
        };

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


    // 기본 로그아웃 - 토큰만 만료
    public void logout(String accessToken) {

        // kakaoUtil.logout(accessToken);

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
