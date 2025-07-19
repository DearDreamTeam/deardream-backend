package com.deardream.deardream_be.domain.family.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FamilyInvitationDto {
    private String leaderName;
    private String recipientName;
}
