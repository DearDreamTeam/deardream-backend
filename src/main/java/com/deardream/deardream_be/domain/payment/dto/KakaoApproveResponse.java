package com.deardream.deardream_be.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class KakaoApproveResponse {
    private String aid; // 결제 승인 고유 번호
    private String tid; // 결제 고유 번호
    private String cid; // 가맹점 코드
    private String sid; // 정기 결제 ID (정기 결제의 경우에만 존재)
    private String partner_order_id; // 가맹점 주문 번호
    private String partner_user_id; // 가맹점 회원 ID
    private String payment_method_type; // 결제 수단
    private int quantity; // 결제 금액
    private Amount amount; // 결제 금액
    private String item_name; // 상품 이름
    private String item_code; // 상품 코드
    private String created_at; // 결제 요청 시간
    private String approved_at; // 결제 승인 시간
    private String payload; // 결제 승인 시 전달되는 추가 정보
}
