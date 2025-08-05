package com.deardream.deardream_be.domain.family.service;

import com.deardream.deardream_be.domain.family.dto.FamilyInvitationDto;
import com.deardream.deardream_be.domain.family.dto.FamilyMembersResponseDto;
import com.deardream.deardream_be.domain.family.dto.FamilyResponseDto;
import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.post.Post;
import com.deardream.deardream_be.domain.post.repository.PostRepository;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import com.deardream.deardream_be.domain.recipient.repository.RecipientRepository;
import com.deardream.deardream_be.domain.user.Relation;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PostRepository postRepository;

    @Override
    @Transactional
    // 결제하기 버튼 누르면 createFamily 함
    // 가족 생성 (role = LEADER)
    public FamilyResponseDto createMyFamily(Long kakaoId) {
        // 1. 사용자 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 2. 이미 가족이 있으면 예외
        if (user.getFamily() != null) {
            throw new GeneralException(ErrorStatus._FAMILY_ALREADY_REGISTERED);
        }

        // 대표자 relation 기본 설정
        user.initializeLeaderRelation();

        // 3. Family 엔티티 초기화
        Family family = new Family();
        family.startFamilyRegistration(user, null);

        // 4. 저장
        Family tempFamilySaved = familyRepository.save(family);

        // 5. 유저와 연관관계 매핑
        user.joinFamilyAsLeader(tempFamilySaved);
        userRepository.save(user);

        // 6. family 관련 작업
        Recipient recipient = recipientRepository.findByLeaderId(user.getId()).orElse(null);
        if (recipient != null) {
            // 6-1. 대표자의 recipient가 있다면 familyId 연동
            recipient.assignFamily(tempFamilySaved);
            recipientRepository.save(recipient);

            // 6-2. 해당 userId를 leaderId로 가진 recipient의 DeliveryType 조회
            DeliveryType recipientDeliveryType = recipient.getDeliveryType();
            // 기관 플랜일 시 : isActive, hasSubscribed true 세팅 (기본값은 false)
            if(recipientDeliveryType == DeliveryType.INSTITUTION) {
                tempFamilySaved.setFamilyActive();
            }
            familyRepository.save(tempFamilySaved);
        }

//        // 6-1. (한혜수) 받는 분의 플랜이 기관/가정(가정은 결제가 우선이므로)이라면 바로 Active
//        tempFamilySaved.setFamilyActive();
//        familyRepository.save(tempFamilySaved);

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

        // 3-1. 해당 familyId를 가진 leaderId 조회 (리더 조회)
        User leader = family.getLeader();
        String leaderName = leader != null ? leader.getName() : null;

        // 3-2. 해당 leaderId를 가진 수신자 조회(수신자 조회)
        String recipientName = null;
        if (leader != null) {
            recipientName = recipientRepository.findByLeaderId(leader.getId())
                    .map(Recipient::getName)
                    .orElse(null);
        }

        // 3-3. 수신자 이미지 반환
        String recipientProfileImage = null;
        if (leader != null) {
            recipientProfileImage = recipientRepository.findByLeaderId(leader.getId())
                    .map(Recipient::getProfileImage)
                    .orElse(null);
        }

        // 4. dto에 담아 반환
        return FamilyMembersResponseDto.of(family, familyMembers, leaderName, recipientName, recipientProfileImage);
    }


    @Override
    @Transactional
    // 로그인 전 초대장 받았을 때 가족 초대장 조회 api
    public FamilyInvitationDto getMyFamilyInvitation(String inviteCode) {
        Family family = familyRepository.findByFamilyLink(inviteCode)
                .orElseThrow(() -> new GeneralException(ErrorStatus._INVALID_INVITE_LINK));

        User leader = family.getLeader();
        String leaderName = leader != null ? leader.getName() : null;

        String recipientName = null;
        if (leader != null) {
            recipientName = recipientRepository.findByLeaderId(leader.getId())
                    .map(Recipient::getName)
                    .orElse(null);
        }

        return FamilyInvitationDto.builder()
                .leaderName(leaderName)
                .recipientName(recipientName)
                .build();
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

        // 2-1. 예외처리 : 이미 familyLink가 있다면 예외 발생
        if(inviteLinkToken != null) {
            throw new GeneralException(ErrorStatus._INVITE_LINK_ALREADY_EXISTS);
        }

        // 2-2. 예외처리 : 활성화가 안 된 가족일 시 예외 발생 - 근데 프론트에서 가족 링크 생성은 되게 해달라고 해서
        // 예외처리는 품
//        if(family.getIsActive() == false){
//            throw new GeneralException(ErrorStatus._FAMILY_NOT_ACTIVE);
//        }

        // 3. familyLink가 없을 때만 새로 생성
        inviteLinkToken = UUID.randomUUID().toString();
        family.updateFamilyInviteLink(inviteLinkToken);
        familyRepository.save(family);

        return inviteLinkToken;
    }

    @Override
    @Transactional
    public String getInviteLink(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        Family family = user.getFamily();
        if (family == null) {
            throw new GeneralException(ErrorStatus._FAMILY_NOT_FOUND);
        }

        if(family.getIsActive() == false){
            throw new GeneralException(ErrorStatus._FAMILY_NOT_ACTIVE);
        }

        return family.getFamilyLink();
    }


    @Override
    @Transactional
    // 초대 링크로 가입(role : USER) -> 이미 가입된 유저가 초대 그룹에 USER로 합류
    public void joinByInviteCode(String inviteCode, Long kakaoId) {
        // 1. 초대코드로 Family 조회 (familyId는 family.getId()에 들어있음)
        Family family = familyRepository.findByFamilyLink(inviteCode)
                .orElseThrow(() -> new GeneralException(ErrorStatus._INVALID_INVITE_LINK));

        // 2. kakaoId로 가입된 사용자 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 3. 이미 가족에 속해 있는 경우 케이스 분기
        Family myFamily = user.getFamily();
        if(myFamily != null) {
            if(myFamily.getIsActive() == true && myFamily.getHasSubscribed() == true){
                throw new GeneralException(ErrorStatus._ALREADY_IN_ACTIVE_FAMILY);
            }
            else if(!myFamily.getIsActive() && myFamily.getHasSubscribed() == true){
                throw new GeneralException(ErrorStatus._FAMILY_NOT_ACTIVE);
            }
            else if(!myFamily.getIsActive() && !myFamily.getHasSubscribed()){
                deleteTemporaryFamily(user);
            }
        }

//        if (user.getFamily() != null) {
//            throw new GeneralException(ErrorStatus._ALREADY_IN_FAMILY);
//        }

        // 4. User 쪽에 familyId 설정
        user.joinFamilyAsUser(family);

        // 5. 저장하면 user.familyId 칼럼에 자동으로 family.getId 반영
        userRepository.save(user);
    }


    // 결제하지 않은 leader가 생성한 임시 가족을 삭제하는 로직 - 가족에 leader만 들어가 있고 다른 user는 없는 상태
    // leader만 familyId null처리 하고 family를 삭제하면 됨
    // 가족과 수신자만 생성되어있음 (결제하지 않았을 시 post 작성 불가여서, 가족에 딸린 정보는 수신자밖에 없음)
    // 가족과 수신자는 cascade 되어 있음, 가족만 삭제하면 수신자 삭제 됨
    @Transactional
    public void deleteTemporaryFamily(User leader) {
        Family family = leader.getFamily();

        // 1. leader의 familyId null 처리
        leader.deleteFamily();
        userRepository.save(leader);

        // 2. family 삭제
        familyRepository.delete(family);

    }

    // 테스트용 임시 로직
    @Transactional
    public void deleteFamily(Long familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        // 가족의 모든 멤버를 조회
        List<User> familyMembers = userRepository.findAllByFamilyId(family.getId());

        List<Post> posts = postRepository.findAllByFamily(family);

        // 가족의 모든 게시글 삭제
        for (Post post : posts) {
            post.deleteFamily();
        }

        // 가족의 모든 멤버에서 가족 정보 제거
        for (User member : familyMembers) {
            member.deleteFamily();
        }

        // 가족 삭제
        familyRepository.delete(family);

    }

}