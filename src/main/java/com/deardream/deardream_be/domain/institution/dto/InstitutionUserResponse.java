package com.deardream.deardream_be.domain.institution.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class InstitutionUserResponse {
    private Long familyId;
    private Long userId;
    private String recipientName;

}
