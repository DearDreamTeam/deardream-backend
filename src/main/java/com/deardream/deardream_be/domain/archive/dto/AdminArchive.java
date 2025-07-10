package com.deardream.deardream_be.domain.archive.dto;

import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminArchive {

    private List<AdminHomeArchive> homeArchives;
    private List<AdminInstitutionArchive> institutionArchives;

    @Builder
    @AllArgsConstructor
    @Getter
    public static class AdminHomeArchive {
        private Long archiveId;
        private Long familyId;
        private String receiverName;
        private String address1;
        private String address2;
        private String zipCode;
        private String phone;
        private String pdfUrl;
        private DeliveryStatus deliveryStatus;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class AdminInstitutionArchive {
        private Long institutionId;
        private String institutionCode;
        private String institutionName;
        private String address1;
        private String zipCode;
        private String phone;
        private DeliveryStatus deliveryStatus;
        private List<AdminFamilyInfo> families;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class AdminFamilyInfo {
        private Long archiveId;
        private Long familyId;
        private String receiverName;
        private String address2;
        private String pdfUrl;
    }

}
