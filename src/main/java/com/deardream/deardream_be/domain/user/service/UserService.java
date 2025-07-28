package com.deardream.deardream_be.domain.user.service;

import com.deardream.deardream_be.domain.user.dto.RegisterResponseDto;
import com.deardream.deardream_be.domain.user.dto.UserRequestDto;
import com.deardream.deardream_be.domain.user.dto.UserResponseDto;
import com.deardream.deardream_be.domain.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

// 서비스 인터페이스 정의
// 아래 주석처럼 생긴 것은 JavaDoc 주석
// JavaDoc 툴을 돌리면 이 주석을 기반으로 html 문서가 자동 생성됩니다.
public interface UserService {
    /**
     * 회원 정보 등록
     * @param kakaoId, userRequestDto 등록할 정보
     * @return 등록된 회원 정보
     */
    RegisterResponseDto register(Long kakaoId, UserRequestDto userRequestDto, MultipartFile profileImage, String inviteCode);

    /**
     * 내 정보 조회
     * @param kakaoId
     * @return 조회된 회원 정보
     */
    UserResponseDto getMyInfo(Long kakaoId);

    /**
     * 내 정보 수정
     * @param kakaoId
     * @param userRequestDto 수정할 정보
     * @return 수정된 회원 정보
     */
    UserResponseDto updateMyInfo(Long kakaoId, UserRequestDto userRequestDto, MultipartFile profileImage);

    /**
     * 회원 탈퇴 (로그인된 회원 삭제)
     * @param kakaoId
     */
    void deleteMyAccount(Long kakaoId);


    /**
     * userId로 familyId 찾기
     * @param userId
     * @return
     */
    Long getFamilyIdByUserId(Long userId);

}
