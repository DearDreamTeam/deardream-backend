package com.deardream.deardream_be.domain.user.entity;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.user.Relation;
import com.deardream.deardream_be.domain.institution.CalendarType;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.domain.user.dto.UserRequestDto;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "`user`")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "kakao_id")
    private Long kakaoId;

    @Column(name = "name")
    private String name;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "profile_image_key")
    private String profileImageKey;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "calendar_type")
    @Enumerated(EnumType.STRING)
    private CalendarType calendarType;

    @Column(name = "relation")
    @Enumerated(EnumType.STRING)
    private Relation relation;

    @Column(name = "other_relation")
    private String otherRelation;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    // 프로필 등록 완료 여부
    @Column(name = "is_registered", nullable = false)
    private boolean isRegistered = false;


    // 유저 등록
    public void completeRegistration(UserRequestDto dto, Family family, Role assignedRole, String profileImageUrl, String profileImageKey) {
        this.name = dto.getName();
        this.profileImageUrl = profileImageUrl;
        this.profileImageKey = profileImageKey;
        this.birth = dto.getBirth();
        this.calendarType = dto.getCalendarType();
        this.relation = dto.getRelation();
        this.otherRelation = dto.getOtherRelation();
        this.family = family;
        this.role = assignedRole;
        this.isRegistered = true;
    }

    // 유저 수정
    public void updateUserInfo(UserRequestDto dto, String profileImageUrl, String profileImageKey) {
        if (dto.getName() != null) this.name = dto.getName();
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
        if (profileImageKey != null) this.profileImageKey = profileImageKey;
        if (dto.getCalendarType() != null) this.calendarType = dto.getCalendarType();
        if (dto.getBirth() != null) this.birth = dto.getBirth();
        if (dto.getRelation() != null) this.relation = dto.getRelation();
        if (dto.getOtherRelation() != null) this.otherRelation = dto.getOtherRelation();
    }

    // 가족 생성하고 리더로 합류할 때 호출
    public void joinFamilyAsLeader(Family family) {
        this.family = family;
        this.role = Role.LEADER;
    }

    // 초대 링크로 가입된 멤버를 처리할 때 호출 (아직 사용 x)
    public void joinFamilyAsUser(Family family) {
        this.family = family;
        this.role = Role.USER;
    }
}
