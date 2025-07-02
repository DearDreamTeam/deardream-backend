package com.deardream.deardream_be.domain.archive.dto;

import com.deardream.deardream_be.domain.archive.DeliveryStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminArchive {
    // 가족 ID, 수신자 성명, 주소1, 주소2, 우편번호, 받는 분 전화번호, PDF URL, 현재 배송 상태
    private Long familyId;
    private String recipientName;
    private String address1;
    private String address2;
    private String postalCode;
    private String recipientPhoneNumber;
    private String pdfUrl;
    private DeliveryStatus deliveryStatus;

}
