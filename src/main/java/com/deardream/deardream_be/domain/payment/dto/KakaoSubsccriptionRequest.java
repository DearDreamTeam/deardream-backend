package com.deardream.deardream_be.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoSubsccriptionRequest {
    private String cid; // 가맹점 코드
    private String sid; // 정기 결제 ID
    private String partnerOrderId; // 가맹점 주문 번호
    private String partnerUserId; // 가맹점 회원 ID
    private String itemName; // 상품 이름
    private int quantity; // 상품 수량
    private int totalAmount; // 총 결제 금액
    private int taxFreeAmount; // 면세 금액

}
