package com.deardream.deardream_be.domain.family.service;

import com.deardream.deardream_be.domain.family.dto.FamilyInvitationDto;
import com.deardream.deardream_be.domain.family.dto.FamilyMembersResponseDto;
import com.deardream.deardream_be.domain.family.dto.FamilyResponseDto;

public interface FamilyService {

    // 새로운 가족 그룹 생성하고 leader 권한 부여
    FamilyResponseDto createFamily(Long userId);

    // 내 가족 정보 조회
    FamilyMembersResponseDto getMyFamily(Long kakaoId);

    // 내 가족 초대장 조회
    FamilyInvitationDto getMyFamilyInvitation(String inviteCode);

    // 초대 링크 생성
    String createInviteLink(Long kakaoId);

    // 초대 링크 조회
    String getInviteLink(Long kakaoId);

    // 초대 링크를 통해 가족 가입 (user 권한 부여)
    void joinByInvite(String inviteCode, Long kakaoId);

}
