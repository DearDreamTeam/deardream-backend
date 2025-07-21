package com.deardream.deardream_be.domain.archive.dto;


import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminHomeArchiveResponse {
    private List<HomeArchiveDto> homeArchives;

    @Getter
    @Builder
    public static class HomeArchiveDto {
        // 이름, 주소, 상세 주소, 우편 번호, 받는 분 전화번호, 상태, pdf
        private Long archiveId;
        private String receiverName;
        private String address;
        private String addressDetail;
        private String postalCode;
        private String phone;
        private DeliveryStatus deliveryStatus;
        private String pdfUrl;

    }
}
