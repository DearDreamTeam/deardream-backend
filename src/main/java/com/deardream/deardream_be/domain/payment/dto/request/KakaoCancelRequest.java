package com.deardream.deardream_be.domain.payment.dto.request;

import lombok.Getter;

@Getter
public class KakaoCancelRequest {
    private String tid; // 결제 고유 번호
    private Long familyId;
    private Long orderUserId; // 주문자 ID
}
