package com.deardream.deardream_be.domain.institution;

import lombok.Getter;

@Getter
public enum DeliveryType {
    INSTITUTION("기관"),
    HOME("가정");

    private final String description;

    DeliveryType(String description) {
        this.description = description;
    }

}
