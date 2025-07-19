package com.deardream.deardream_be.domain.family.dto;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.user.dto.UserResponseDto;
import com.deardream.deardream_be.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class FamilyMembersResponseDto {

    private Long familyId;
    private String familyLink;
    private LocalDateTime createdAt;
    private String leaderName;
    private String recipientName;
    private String recipientProfileImage;
    private List<UserResponseDto> members;


    // family엔티티 + 해당 familyId에 속한 User 리스트를
    // UserResponseDto로 매핑한 뒤 dto에 담아 반환함
    public static FamilyMembersResponseDto of(Family family, List<User> users, String leaderName, String recipientName, String recipientProfileImage) {
        List<UserResponseDto> memberDtos = users.stream()
                .map(UserResponseDto::of)
                .collect(Collectors.toList());

        return FamilyMembersResponseDto.builder()
                .familyId(family.getId())
                .familyLink(family.getFamilyLink())
                .createdAt(family.getCreatedAt())
                .members(memberDtos)
                .leaderName(leaderName)
                .recipientName(recipientName)
                .recipientProfileImage(recipientProfileImage)
                .build();
    }
}
