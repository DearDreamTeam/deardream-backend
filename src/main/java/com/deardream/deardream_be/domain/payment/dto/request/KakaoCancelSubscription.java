package com.deardream.deardream_be.domain.payment.dto.request;

import lombok.Getter;

@Getter
public class KakaoCancelSubscription {
    private Long familyId;
    private Long orderUserId;

    public KakaoCancelSubscription build(Long familyId, Long orderUserId) {
        this.familyId = familyId;
        this.orderUserId = orderUserId;
        return this;
    }
}
