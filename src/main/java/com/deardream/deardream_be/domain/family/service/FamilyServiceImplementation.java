package com.deardream.deardream_be.domain.family.service;

import com.deardream.deardream_be.domain.family.dto.FamilyMembersResponseDto;
import com.deardream.deardream_be.domain.family.dto.FamilyRequestDto;
import com.deardream.deardream_be.domain.family.dto.FamilyResponseDto;
import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import com.deardream.deardream_be.domain.recipient.repository.RecipientRepository;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.domain.user.dto.UserResponseDto;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyServiceImplementation implements FamilyService {

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;

    @Override
    @Transactional
    // 결제하기 버튼 누르면 createFamily 함
    // 가족 생성 (role = LEADER)
    public FamilyResponseDto createFamily(Long kakaoId) {
        // 1. 사용자 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 2. 이미 가족이 있으면 예외
        if (user.getFamily() != null) {
            throw new GeneralException(ErrorStatus._FAMILY_ALREADY_REGISTERED);
        }

        // 3. Family 엔티티 초기화
        Family family = new Family();
        family.startFamilyRegistration(user, null);

        // 4. 저장
        Family tempFamilySaved = familyRepository.save(family);

        // 5. 유저와 연관관계 매핑
        user.joinFamilyAsLeader(tempFamilySaved);
        userRepository.save(user);

        // 6. 대표자의 recipient가 있다면 familyId 연동
        Recipient recipient = recipientRepository.findByLeaderId(user.getId()).orElse(null);
        if (recipient != null) {
            recipient.assignFamily(tempFamilySaved);
            recipientRepository.save(recipient);
        }

        return FamilyResponseDto.of(tempFamilySaved);
    }

    @Override
    @Transactional(readOnly = true)
    // 내 가족 조회 (memberList 포함)
    public FamilyMembersResponseDto getMyFamily(Long kakaoId) {
        // 1. 로그인한 유저 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 2. 유저가 속한 가족 ID 얻기
        Family family = user.getFamily();
        if(family == null) {
            throw new GeneralException(ErrorStatus._FAMILY_NOT_FOUND);
        }

        // 3. 해당 familyId를 가진 모든 유저 조회
        List<User> familyMembers = userRepository.findAllByFamilyId(family.getId());

        // 4. dto에 담아 반환
        return FamilyMembersResponseDto.of(family, familyMembers);
    }

    @Override
    @Transactional
    // 새 멤버 초대하기 버튼 누를 때 초대 링크 만들어짐
    // 초대 링크 생성 (role : LEADER)
    public String createInviteLink(Long leaderId) {

        // 1. 리더 여부 확인
        Family family = familyRepository.findByLeaderId(leaderId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MUST_BE_LEADER_TO_DO));

        String inviteLinkToken = family.getFamilyLink();

        // 2. leaderId의 familyLink가 널값인지, 아니면 이미 만들어져 있는지 확인
        if(inviteLinkToken == null) {

            // 3. 초대 토큰 생성
            inviteLinkToken = UUID.randomUUID().toString();

            // 4. 엔티티에 토큰 반영
            family.updateFamilyInviteLink(inviteLinkToken);
            familyRepository.save(family);
        }

        // 5. 이미 있다면 초대 url 반환
//        return String.format("%s/family/join?code=%s", frontendBaseUrl, inviteLinkToken);
            return inviteLinkToken;
    }

    @Override
    @Transactional
    // 초대 링크로 가입(role : USER) -> 이미 가입된 유저가 초대 그룹에 USER로 합류
    public void joinByInvite(String inviteCode, Long kakaoId) {
        // 1. 초대코드로 Family 조회 (familyId는 family.getId()에 들어있음)
        Family family = familyRepository.findByFamilyLink(inviteCode)
                .orElseThrow(() -> new GeneralException(ErrorStatus._INVALID_INVITE_LINK));

        // 2. kakaoId로 가입된 사용자 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 3. 이미 가족에 속해 있는지 확인
        if (user.getFamily() != null) {
            throw new GeneralException(ErrorStatus._ALREADY_IN_FAMILY);
        }

        // 4. User 쪽에 familyId 설정
        user.joinFamilyAsUser(family);

        // 5. 저장하면 user.familyId 칼럼에 자동으로 family.getId 반영
        userRepository.save(user);
    }

}