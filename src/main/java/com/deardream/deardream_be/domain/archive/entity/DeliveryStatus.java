package com.deardream.deardream_be.domain.archive.entity;

import lombok.Getter;

@Getter
public enum DeliveryStatus {
    PENDING("준비 중"),
    DELIVERING("배달 중"),
    DELIVERED("배달 완료");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

}
