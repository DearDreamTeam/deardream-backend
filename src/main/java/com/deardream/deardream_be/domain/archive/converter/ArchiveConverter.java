package com.deardream.deardream_be.domain.archive.converter;

import com.deardream.deardream_be.domain.archive.dto.AdminArchive;
import com.deardream.deardream_be.domain.archive.dto.AdminHomeArchiveResponse;
import com.deardream.deardream_be.domain.archive.dto.AdminInstitutionArchiveResponse;
import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import com.deardream.deardream_be.domain.archive.entity.MonthlyArchive;
import com.deardream.deardream_be.domain.archive.dto.ArchiveResponseDto;
import com.deardream.deardream_be.domain.institution.Institution;
import com.deardream.deardream_be.domain.post.service.PostImageService;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ArchiveConverter {

    private final PostImageService postImageService;

    // ArchiveResponseDto로 변환하는 메서드
    // public ArchiveResponseDto toArchiveResponseDto(MonthlyArchive archive) {
    //     return ArchiveResponseDto.builder()
    //             .yearMonthType(archive.getYearMonthType())
    //             .pdfUrl(archive.getPdfUrl())
    //             .build();
    // }

    public ArchiveResponseDto toArchiveResponse(MonthlyArchive archive) {
        String yearMonth = archive.getArchiveYear() + "년" + archive.getArchiveMonth() + "월";

        return ArchiveResponseDto.builder()
                .yearMonthType(yearMonth)
                .pdfUrl(archive.getPdfUrl())
                .build();
    }

    public AdminArchive.AdminInstitutionArchive toInstitutionArchive(Institution institution,
                                                                  DeliveryStatus deliveryStatus,
                                                                  List<AdminArchive.AdminFamilyInfo> familyInfoList) {
        return AdminArchive.AdminInstitutionArchive.builder()
                .institutionId(institution.getId())
                .institutionCode(institution.getCode())
                .institutionName(institution.getName())
                .address(institution.getAddress())
                .postalCode(institution.getPostalCode())
                .phone(institution.getPhone())
                .deliveryStatus(deliveryStatus)
                .families(familyInfoList)
                .build();
    }

    public AdminArchive.AdminFamilyInfo toInstitutionInfo(MonthlyArchive archive) {
        Recipient recipient = archive.getRecipient();

        return AdminArchive.AdminFamilyInfo.builder()
                .archiveId(archive.getId())
                .familyId(archive.getFamily().getId())
                .receiverName(recipient.getName())
                .addressDetail(recipient.getAddressDetail())
                .pdfUrl(postImageService.getFilesUrl(archive.getS3Key()))
                .build();
    }

    public AdminArchive.AdminHomeArchive toHomeArchive(MonthlyArchive archive) {
        Recipient recipient = archive.getRecipient();

        return AdminArchive.AdminHomeArchive.builder()
                .archiveId(archive.getId())
                .familyId(archive.getFamily().getId())
                .receiverName(recipient.getName())
                .address(recipient.getAddress())
                .addressDetail(recipient.getAddressDetail())
                .postalCode(recipient.getPostalCode())
                .phone(recipient.getPhone())
                .pdfUrl(postImageService.getFilesUrl(archive.getS3Key()))
                .deliveryStatus(archive.getDeliveryStatus())
                .build();

    }

    public AdminHomeArchiveResponse.HomeArchiveDto toHomeArchiveResponse(MonthlyArchive archive) {
        Recipient recipient = archive.getRecipient();

        return AdminHomeArchiveResponse.HomeArchiveDto.builder()
                .archiveId(archive.getId())
                .receiverName(recipient.getName())
                .address(recipient.getAddress())
                .addressDetail(recipient.getAddressDetail())
                .postalCode(recipient.getPostalCode())
                .phone(recipient.getPhone())
                .deliveryStatus(archive.getDeliveryStatus())
                .pdfUrl(postImageService.getFilesUrl(archive.getS3Key()))
                .build();

    }

    public AdminInstitutionArchiveResponse.InstitutionArchiveDto toInstitutionArchiveResponse(MonthlyArchive archive) {
        Recipient recipient = archive.getRecipient();

        return AdminInstitutionArchiveResponse.InstitutionArchiveDto.builder()
                .familyId(archive.getFamily().getId())
                .receiverName(recipient.getName())
                .address(recipient.getAddress())
                .addressDetail(recipient.getAddressDetail())
                .phone(recipient.getPhone())
                .pdfUrl(postImageService.getFilesUrl(archive.getS3Key()))
                .build();
    }



}
