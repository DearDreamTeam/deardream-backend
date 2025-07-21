package com.deardream.deardream_be.domain.institution.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateInstitutionDto {

    @NotNull
    private String name;

    @NotNull
    private String address;

    private String phone;

    @NotNull
    private String postalCode;

    @NotNull
    private String startDate;

    private Integer membersCount;
}
