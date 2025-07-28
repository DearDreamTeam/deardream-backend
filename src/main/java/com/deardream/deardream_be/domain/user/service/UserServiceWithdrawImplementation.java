package com.deardream.deardream_be.domain.user.service;


import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.post.repository.PostRepository;
import com.deardream.deardream_be.domain.recipient.repository.RecipientRepository;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.domain.archive.repository.ArchiveRepository;
import com.deardream.deardream_be.domain.archive.repository.BookmarkRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceWithdrawImplementation implements UserServiceWithdraw {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final PostRepository postRepository;
    private final RecipientRepository recipientRepository;
    private final ArchiveRepository archiveRepository;

    // == 회원 탈퇴 메인 메서드 ==
    @Transactional
    @Override
    public void withdraw(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        Role role = user.getRole();

        if (role == Role.LEADER) {
            withdrawFamilyLeader(user);
        } else if (role == Role.USER) {
            withdrawFamilyUser(user);
        } else if (role == Role.DEFAULT) {
            withdrawDefaultUser(user);
        } else if (role == Role.ADMIN) {
            withdrawAdminUser(user);
        }
    }

    // == 1. 가족 LEADER 탈퇴 ==
    @Transactional
    protected void withdrawFamilyLeader(User leader) {
        Family family = leader.getFamily();

        // 0. (선행: 결제/구독 해지는 이미 처리되었다고 가정)

//        // 1. post 삭제 - post_image는 cascade
//        postRepository.deleteAllByFamily(family);
//
//        // 2. monthly_archive 삭제 - archive_bookmark는 cascade
//        archiveRepository.deleteAllByFamily(family);
//
//        // 3. recipient 삭제
//        recipientRepository.deleteAllByFamily(family);
//
//        log.info("가족 삭제 전 모든 하위 데이터들 삭제 성공");


        // 1. 가족 소속 USER들 : familyId null, role DEFAULT로 변경(본인 제외) (위 데이터 모두 정리 후)
        List<User> users = userRepository.findAllByFamily(family);
        users.remove(leader);
        for (User user : users) {
            user.withdrawMakeFamilyMembersDefault();
        }
        userRepository.saveAll(users);

        // 2. leader의 familyId null 처리
        leader.deleteFamily();
        userRepository.save(leader);

        // 3. family 삭제
        familyRepository.delete(family);

        // 4. 본인 LEADER USER 삭제
        userRepository.delete(leader);

    }

    // == 2. 가족 USER(구성원) 탈퇴 ==
    @Transactional
    protected void withdrawFamilyUser(User user) {
        user.deleteFamily(); // family FK null 처리

        // 작성한 게시글의 author_id를 null로 설정
        postRepository.updateAuthorIdToNull(user.getId());

        userRepository.delete(user);

    }

    // == 3. DEFAULT (가족 없음, 가입자) 탈퇴 ==
    @Transactional
    protected void withdrawDefaultUser(User user) {
        userRepository.delete(user);
    }

    // == 4. ADMIN (관리자, 가입자) 탈퇴 ==
    @Transactional
    protected void withdrawAdminUser(User user) {
        userRepository.delete(user);
    }
}
