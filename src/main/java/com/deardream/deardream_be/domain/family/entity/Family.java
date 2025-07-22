package com.deardream.deardream_be.domain.family.entity;

import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "family")
public class Family extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "leader_id")
    private User leader;

    private String familyLink;

    // 가족 등록
    public void startFamilyRegistration(User leader, String familyLink) {
        this.leader = leader;
        this.familyLink = familyLink;
    }

    // 사용하지 않는 코드, 삭제하기 전 주석처리 하여 기능에 영향이 없는지 한 번 더 확인 중
//    // 리더와 초대링크를 받아 family 생성
//    public static Family createWithLeader(User leader, String familyLink) {
//        Family family = new Family();
//        family.leader     = leader;
//        family.familyLink = familyLink;
//
//        // User에도 연관관계 세팅
//        leader.joinFamilyAsLeader(family);
//        return family;

    // familyLink 업데이트
    public void updateFamilyInviteLink(String inviteLinkToken) {
        this.familyLink = inviteLinkToken;
    }
}