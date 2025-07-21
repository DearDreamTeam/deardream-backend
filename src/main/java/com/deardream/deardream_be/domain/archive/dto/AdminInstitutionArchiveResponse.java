package com.deardream.deardream_be.domain.archive.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminInstitutionArchiveResponse {
    private List<InstitutionArchiveDto> institutions;

    @Getter
    @Builder
    public static class InstitutionArchiveDto {
        private Long familyId;
        private String receiverName;
        private String address;
        private String addressDetail;
        private String phone;
        private String pdfUrl;
    }
}
