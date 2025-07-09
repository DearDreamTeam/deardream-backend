package com.deardream.deardream_be.domain.family.dto;

import com.deardream.deardream_be.domain.family.entity.Family;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class FamilyResponseDto {
        private Long id;
        private String familyLink;
        private Long leaderId;
        private LocalDateTime createdAt;


        // 엔티티 -> dto
        public static FamilyResponseDto of(Family family) {
                return FamilyResponseDto.builder()
                        .id(family.getId())
                        .familyLink(family.getFamilyLink())
                        .leaderId(family.getLeader().getId())
                        .createdAt(family.getCreatedAt())
                        .build();
        }
}