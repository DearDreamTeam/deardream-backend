package com.deardream.deardream_be.domain.user.service;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.jwt.JwtUtil;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.domain.user.dto.RegisterResponseDto;
import com.deardream.deardream_be.domain.user.dto.UserRequestDto;
import com.deardream.deardream_be.domain.user.dto.UserResponseDto;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.deardream.deardream_be.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;
    private final long REFRESH_EXP_TIME = 1000 * 60 * 60 * 24 * 7L;

    @Override
    @Transactional
    public RegisterResponseDto register(Long kakaoId, UserRequestDto userRequestDto) {
        // 카카오 ID로 이미 존재하는 사용자 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                        User stub = User.builder()
                        .kakaoId(kakaoId)
                        .isRegistered(false)
                        .build();
                return userRepository.save(stub);
                });

        // 1. 이미 가입된 kakaoId 중복 체크
        if(user.isRegistered()) {
            throw new GeneralException(ErrorStatus._USER_AlREADY_REGISTERED);
        }

        // 만약 userRequestDto에 familyId가 있다면 -> familyId 저장 / role = user
        // 만약 userRequestDto에 familyId가 없다면 -> role = default
        Family family = null;
        Role assignedRole;
        if (userRequestDto.getFamilyId() != null) {
            family = familyRepository.findById(userRequestDto.getFamilyId())
                    .orElseThrow(()-> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));
            assignedRole = Role.USER;
        } else {
            assignedRole = Role.DEFAULT;
        }

        // 3. 프로필 등록 완료
        user.completeRegistration(userRequestDto, family, assignedRole);

        // 4. jwt 토큰 발급
        String accessToken = jwtUtil.createAccessToken(user.getKakaoId(), user.getRole(), user.getId());
        String refreshToken = jwtUtil.createRefreshToken(user.getKakaoId(), user.getRole(), user.getId());

        // 5. redis에 리프레시 토큰 저장
        redisUtil.setDataExpire("refresh:" + kakaoId, refreshToken, REFRESH_EXP_TIME);

        // 6) 응답 DTO 생성
        return RegisterResponseDto.builder()
                .user(UserResponseDto.of(user))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getMyInfo(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() ->
                        new GeneralException(ErrorStatus._USER_NOT_FOUND)
                );
        return UserResponseDto.of(user);
    }

    @Override
    public UserResponseDto updateMyInfo(Long kakaoId, UserRequestDto userRequestDto) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() ->
                        new GeneralException(ErrorStatus._USER_NOT_FOUND)
                );
        user.updateUserInfo(userRequestDto);
        return UserResponseDto.of(user);
    }

    @Override
    public void deleteMyAccount(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));
        userRepository.delete(user);
    }

    public Long getFamilyIdByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));
        return user.getFamily().getId();
    }
}
