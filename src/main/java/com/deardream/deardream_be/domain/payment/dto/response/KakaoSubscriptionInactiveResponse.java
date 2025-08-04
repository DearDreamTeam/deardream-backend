package com.deardream.deardream_be.domain.payment.dto.response;

import org.joda.time.DateTime;

public class KakaoSubscriptionInactiveResponse {
    private String sid;
    private Long familyId;
    private DateTime inactivatedAt;
}
