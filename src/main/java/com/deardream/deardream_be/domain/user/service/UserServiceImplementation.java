package com.deardream.deardream_be.domain.user.service;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.domain.user.dto.UserRequestDto;
import com.deardream.deardream_be.domain.user.dto.UserResponseDto;
import com.deardream.deardream_be.domain.user.dto.UserUpdateDto;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;

    // 보류
    @Override
    public UserResponseDto register(Long kakaoId, UserRequestDto userRequestDto) {
        // 카카오 ID로 이미 존재하는 사용자 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() ->
                        new EntityNotFoundException("등록되지 않은 카카오 사용자입니다. 카카오 로그인 후 다시 시도하세요.")
                );

        // familyId로 Family 엔티티 조회
        Family family = familyRepository.findById(userRequestDto.getFamilyId())
                .orElseThrow(()-> new EntityNotFoundException("등록되지 않은 가족입니다."));

        // 엔티티에 요청 DTO 값 적용
//        user.updateAdditionalInfo(user);
        return UserResponseDto.of(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getMyInfo(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() ->
                        new EntityNotFoundException("존재하지 않는 회원입니다. ID: " + kakaoId)
                );
        return UserResponseDto.of(user);
    }

    @Override
    public UserResponseDto updateMyInfo(Long kakaoId, UserUpdateDto userUpdateDto) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() ->
                        new EntityNotFoundException("존재하지 않는 회원입니다. ID: " + kakaoId)
                );
        // familyId로 Family 엔티티 조회
//        Family family = familyRepository.findById(userRequestDto.getFamilyId())
//                .orElseThrow(()-> new EntityNotFoundException("등록되지 않은 가족입니다."));

        user.updateAdditionalInfo(userUpdateDto);
        return UserResponseDto.of(user);
    }

    @Override
    public void deleteMyAccount(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다. ID: " + kakaoId));
        userRepository.delete(user);
    }

    public Long getFamilyIdByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return user.getFamily().getId();
    }
}
