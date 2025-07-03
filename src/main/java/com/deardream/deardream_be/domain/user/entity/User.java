package com.deardream.deardream_be.domain.user.entity;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.user.Relation;
import com.deardream.deardream_be.domain.institution.CalendarType;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.domain.user.dto.UserRequestDto;
import com.deardream.deardream_be.domain.user.dto.UserUpdateDto;
import com.deardream.deardream_be.global.apiPayload.exception.OnProfileUpdateValidation;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @NotNull
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "profile_image")
    private String profileImage;

    @NotNull(groups = OnProfileUpdateValidation.class, message = "생년월일은 필수입니다.")
    @Column(name = "birth")
    private LocalDate birth;

    @NotNull(groups = OnProfileUpdateValidation.class, message = "양력/음력 선택은 필수입니다.")
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

    @NotNull
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    // userRequestDto + 변경값 -> User 엔티티에 적용하여 사용자 정보 업데이트
    public void updateAdditionalInfo(UserUpdateDto dto) {
        if (dto.getName() != null) this.name = dto.getName();
        if (dto.getProfileImage() != null) this.profileImage = dto.getProfileImage();
        if (dto.getCalendarType() != null) this.calendarType = dto.getCalendarType();
        if (dto.getBirth() != null) this.birth = dto.getBirth();
        if (dto.getRelation() != null) this.relation = dto.getRelation();
        if (dto.getOtherRelation() != null) this.otherRelation = dto.getOtherRelation();
    }

}
