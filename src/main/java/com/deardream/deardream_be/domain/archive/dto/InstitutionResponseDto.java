package com.deardream.deardream_be.domain.archive.dto;

import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InstitutionResponseDto {
    private List<InstitutionInfo> institutionInfoList;

    @Getter
    @Builder
    public static class InstitutionInfo {
        private Long institutionId;
        private String name;
        private String code;
        private int currentMembers;
        private int nextMembers;
        private String address;
        private String postalCode;
        private String phone;
        private DeliveryStatus deliveryStatus;
        private String pdfUrl = null;
    }
}
