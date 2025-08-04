package com.deardream.deardream_be.domain.payment.dto.response;

import lombok.Getter;
import lombok.ToString;
import org.joda.time.DateTime;

@Getter
@ToString
public class KakaoSubscriptionResponse {
    private String tid;
    private String cid;
    private String sid;
    private String partnerOrderId;
    private String status;
    private DateTime approvedAt;
}
