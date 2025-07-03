package com.deardream.deardream_be.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class Amount {
    private int total;  // 총 결제 금액
    private int tax_free; // 면세 금액
    private int vat; // 부가세 금액
    private int point; // 사용한 포인트 금액
    private int discount; // 할인 금액
    private int green_deposit; // 환경 보호 기부금 (녹색 기부금)
}
