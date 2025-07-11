package com.deardream.deardream_be.domain.institution.service;

import com.deardream.deardream_be.domain.institution.Institution;
import com.deardream.deardream_be.domain.institution.InstitutionRepository;
import com.deardream.deardream_be.domain.institution.dto.CreateInstitutionDto;
import com.deardream.deardream_be.domain.institution.dto.InstitutionResponseDto;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    // Institution 정보 요청
    public InstitutionResponseDto getInstitutionInfo(String code) {
        Institution institution = institutionRepository.findByCode(code)
                .orElseThrow(() -> new GeneralException(ErrorStatus._INSTITUTION_NOT_FOUND));



        return InstitutionResponseDto.builder()
                .code(institution.getCode())
                .name(institution.getName())
                .address(institution.getAddress())
                .phone(institution.getPhone())
                .postalCode(institution.getPostalCode())
                .build();

    }


    // Institution random code 생성
    public String generateInstitutionCode(CreateInstitutionDto request) {

        if(institutionRepository.existsByNameAndAddress(request.getName(), request.getAddress())) {
            throw new GeneralException(ErrorStatus._INSTITUTION_ALREADY_EXISTS);
        }

        String code = generateRandomCode(request.getName(), request.getPostalCode());

        if(institutionRepository.existsByCode(code)) {
                throw new GeneralException(ErrorStatus._INSTITUTION_CODE_ALREADY_EXISTS);
        }

        Institution institution = Institution.builder()
                    .name(request.getName())
                    .address(request.getAddress())
                    .phone(request.getPhone())
                    .postalCode(request.getPostalCode())
                    .code(code)
                    .build();

        institutionRepository.save(institution);

        return code;
    }

    public List<InstitutionResponseDto> getAllInstitutionInfo() {
        List<Institution> institutionList = institutionRepository.findAll();

        return institutionList.stream().map(
                institution -> InstitutionResponseDto.builder()
                        .code(institution.getCode())
                        .name(institution.getName())
                        .address(institution.getAddress())
                        .phone(institution.getPhone())
                        .postalCode(institution.getPostalCode())
                        .build()
        ).toList();
    }


    private String generateRandomCode(String name, String postalCode) {

        if(name.length() < 3) {
            name += name;
        }

        return name.substring(0,3)
                + "-"
                +  postalCode.substring(1,3);
    }

}
