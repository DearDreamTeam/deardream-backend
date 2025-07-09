package com.deardream.deardream_be.domain.family.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FamilyRequestDto {
    private Long id;
    private String familyLink;
    private Long leaderId;
    private LocalDateTime createdAt;
}
