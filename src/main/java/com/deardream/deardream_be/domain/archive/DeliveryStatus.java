package com.deardream.deardream_be.domain.archive;

import lombok.Getter;

@Getter
public enum DeliveryStatus {
    PENDING("대기 중"),
    DELIVERED("배달 완료"),
    FAILED("배달 실패");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

}
