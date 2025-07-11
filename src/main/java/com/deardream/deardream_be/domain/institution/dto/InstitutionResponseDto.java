package com.deardream.deardream_be.domain.institution.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class InstitutionResponseDto {
    private String code;
    private String name;
    private String address;
    private String phone;
    private String postalCode;
}
