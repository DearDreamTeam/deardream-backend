package com.deardream.deardream_be.domain.user.dto;

import com.deardream.deardream_be.domain.institution.CalendarType;
import com.deardream.deardream_be.domain.user.Relation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    private String name;
    private String profileImage;
    private LocalDate birth;
    private CalendarType calendarType;
    private Relation relation;
    private String otherRelation;

}
