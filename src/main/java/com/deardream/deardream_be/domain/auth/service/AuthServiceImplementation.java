package com.deardream.deardream_be.domain.auth.service;

import com.deardream.deardream_be.domain.auth.dto.KakaoDto;
import com.deardream.deardream_be.domain.auth.util.KakaoUtil;
//import com.deardream.deardream_be.domain.jwt.util.JwtUtil;
import com.deardream.deardream_be.domain.jwt.JwtUtil;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImplementation implements AuthService {

    private final KakaoUtil kakaoUtil;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public User loginWithKakao(String code, Long familyId) {
        // 1. 카카오에서 access token 요청
        KakaoDto.OAuthToken tokenResponse = kakaoUtil.getAccessToken(code);

        // 2. 카카오에서 사용자 정보 요청
        KakaoDto.KakaoProfile kakaoProfile = kakaoUtil.getUserInfo(tokenResponse.getAccess_token());
        Long kakaoId = kakaoProfile.getId();
        String email = kakaoProfile.getKakao_account().getEmail();
        String profileImage = kakaoProfile.getKakao_account().getProfile().getProfile_image_url();
        String name = kakaoProfile.getKakao_account().getName() != null
                ? kakaoProfile.getKakao_account().getName()
                : kakaoProfile.getProperties().getNickname();

        // 3. kakaoId를 통해 기존 유저 확인 또는 신규 유저 등록(신규 유저인 경우 role 분기)
        // familyId 존재 -> role : USER , familyId 없음 -> role : LEADER
//        return userRepository.findByKakaoId(kakaoId)
//                .orElseGet(() ->
//                {
//                    Role role = (familyId != null) ? Role.USER : Role.LEADER;
//                    return userRepository.save(
//                            User.builder()
//                                    .kakaoId(kakaoId)
//                                    .name(name)
//                                    .profileImage(profileImage)
//                                    .email(email)
//                                    .role(role) // 분기된 role 저장
//                                    .build()
//                    );
//                });

        try {
            User user = userRepository.findByKakaoId(kakaoId)
                    .orElseGet(() -> {
                        Role role = (familyId != null) ? Role.USER : Role.LEADER;
                        User newUser = User.builder()
                                .kakaoId(kakaoId)
                                .name(name)
                                .profileImage(profileImage)
                                .email(email)
                                .role(role)
                                .build();
                        log.info("신규 User 저장 시도: {}", newUser);
                        return userRepository.save(newUser);
                    });
            log.info("User 저장/조회 성공: {}", user);
            return user;
        } catch (Exception e) {
            log.error("User 엔티티 저장/조회 중 예외 발생", e);
            throw e;
        }
    }

    @Override
    public String generateAccessToken(User user) { return jwtUtil.createAccessToken(user.getKakaoId(), user.getRole(), user.getId());}

    @Override
    public String generateRefreshToken(User user) {
        return jwtUtil.createRefreshToken(user.getKakaoId(), user.getRole(), user.getId());
    }
}
