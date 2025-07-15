package com.deardream.deardream_be.domain.user.service;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.jwt.JwtUtil;
import com.deardream.deardream_be.domain.post.service.PostImageService;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.domain.user.dto.RegisterResponseDto;
import com.deardream.deardream_be.domain.user.dto.UserRequestDto;
import com.deardream.deardream_be.domain.user.dto.UserResponseDto;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.deardream.deardream_be.global.common.UploadResult;
import com.deardream.deardream_be.global.config.S3Config;
import com.deardream.deardream_be.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImplementation implements UserService {

    private final S3Config s3Config;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;
    private final long REFRESH_EXP_TIME = 1000 * 60 * 60 * 24 * 7L;
    private final PostImageService postImageService;

    @Override
    @Transactional
    public RegisterResponseDto register(Long kakaoId, UserRequestDto userRequestDto, MultipartFile profileImage, String inviteCode) {
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


        // 2. 초대 코드가 있으면 USER / 초대 코드가 없으면 DEFAULT
        Family family = null;
        Role assignedRole = Role.DEFAULT;

        if (inviteCode != null && !inviteCode.isEmpty()) {
            family = familyRepository.findByFamilyLink(inviteCode)
                    .orElseThrow(()-> new GeneralException(ErrorStatus._INVALID_INVITE_LINK));
            assignedRole = Role.USER;
        }

        // 3. S3에 이미지 등록 및 프로필 등록 완료
        String profileImageUrl = null;
        String profileImageKey = null;

        if(profileImage != null && !profileImage.isEmpty()) {
            validateProfileImage(profileImage);

            String fileName = "profile_" + kakaoId + "_" + System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
            UploadResult uploadResult = postImageService.uploadFile(s3Config.getProfileFolder(), fileName, profileImage);

            profileImageKey = uploadResult.getKey();
            profileImageUrl = postImageService.getFilesUrl(profileImageKey);
        }

        // 4. 프로필 등록 완료
        user.completeRegistration(userRequestDto, family, assignedRole, profileImageUrl, profileImageKey);

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
    public UserResponseDto updateMyInfo(Long kakaoId, UserRequestDto userRequestDto, MultipartFile profileImage) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() ->
                        new GeneralException(ErrorStatus._USER_NOT_FOUND)
                );
        // 기존 프로필 이미지 삭제
        if(user.getProfileImageKey() != null) {
            postImageService.deleteFile(user.getProfileImageKey());
        }
        // 새 프로필 이미지 업로드
        String profileImageUrl = null;
        String profileImageKey = null;

        if(profileImage != null && !profileImage.isEmpty()) {
            validateProfileImage(profileImage);

            String fileName = "profile_" + kakaoId + "_" + System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
            UploadResult uploadResult = postImageService.uploadFile(s3Config.getProfileFolder(), fileName, profileImage);

            profileImageKey = uploadResult.getKey();
            profileImageUrl = postImageService.getFilesUrl(profileImageKey);
        }

        user.updateUserInfo(userRequestDto, profileImageUrl, profileImageKey);
        return UserResponseDto.of(user);
    }

    @Override
    public void deleteMyAccount(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        if (user.getProfileImageKey() != null) {
            postImageService.deleteFile(user.getProfileImageKey());
        }
        userRepository.delete(user);
    }

    public Long getFamilyIdByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));
//        return user.getFamily().getId();
        Family family = user.getFamily();
        return (family != null) ? family.getId() : null;
    }


    private void validateProfileImage(MultipartFile profileImage) {
        // 파일 크기 제한 (1MB)
        long maxSizeBytes = 1024 * 1024;
        if (profileImage.getSize() > maxSizeBytes) {
            throw new GeneralException(ErrorStatus._IMAGE_SIZE_EXCEEDED);
        }

    }

}
