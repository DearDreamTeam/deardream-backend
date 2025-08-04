package com.deardream.deardream_be.domain.payment.dto.request;

import lombok.Getter;

@Getter
public class KakaoReadyRequestDto {
    private Long familyId;
    private Long orderUserId;
    private String redirectUrl;

}
