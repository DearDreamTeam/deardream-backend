package com.deardream.deardream_be.domain.user.dto;

import com.deardream.deardream_be.domain.institution.CalendarType;
import com.deardream.deardream_be.domain.user.Relation;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.domain.user.entity.User;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponseDto {

    private Long id;
    private Long kakaoId;
    private String name;
    private String profileImage;
    private LocalDate birth;
    private CalendarType calendarType;
    private Relation relation;
    private String otherRelation;
    private Role role;
    private LocalDateTime createdAt;
    private Long familyId;

    // Entity → DTO 변환 편의를 위한 팩토리 메서드
    // 엔티티 -> dto (값 변경 없음)
    public static UserResponseDto of(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .birth(user.getBirth())
                .calendarType(user.getCalendarType())
                .relation(user.getRelation())
                .otherRelation(user.getOtherRelation())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .familyId(user.getFamily() != null ? user.getFamily().getId() : null)
                .build();
    }
}
