package com.deardream.deardream_be.domain.user.dto;

import com.deardream.deardream_be.domain.institution.CalendarType;
import com.deardream.deardream_be.domain.user.Relation;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// UserRequestDto : 회원가입, 등록용 post
public class UserRequestDto {

    @NotNull
    private String name;

//    @NotNull
//    private String profileImage;

    @NotNull
    private LocalDate birth;

    @NotNull
    private CalendarType calendarType;

    private Relation relation;
    private String otherRelation;


    @JoinColumn(name = "family_id")
    private Long familyId;

//    private String familyLink;


}