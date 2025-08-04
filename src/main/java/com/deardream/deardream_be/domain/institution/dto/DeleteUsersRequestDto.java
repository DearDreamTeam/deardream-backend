package com.deardream.deardream_be.domain.institution.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeleteUsersRequestDto {
    @NotNull
    private String code;

    @NotNull
    private Long familyId;
}
