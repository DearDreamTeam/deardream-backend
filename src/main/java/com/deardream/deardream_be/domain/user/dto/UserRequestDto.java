package com.deardream.deardream_be.domain.user.dto;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.institution.CalendarType;
import com.deardream.deardream_be.domain.user.Relation;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.joda.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// UserRequestDto : 회원가입, 등록용 post
public class UserRequestDto extends BaseEntity {

    @NotNull
    private String name;

    @NotNull
    private String profileImage;

    @NotNull
    private LocalDate birth;

    @NotNull
    private CalendarType calendarType;

    private Relation relation;
    private String otherRelation;

    private Role role;

    @JoinColumn(name = "family_id")
    private Long familyId;


}

/*
정보 등록에 필요한 내용

이름 : name
양력음력 : calendarType
생년월일 : birth
받는분과의 관계 -> 지정됐거나/입력하거나 : relation / otherRelation
프로필사진선택 : image


 */