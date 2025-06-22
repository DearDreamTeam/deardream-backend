package com.deardream.deardream_be.domain.institution;

import lombok.Getter;

@Getter
public enum CalendarType {
    SOLAR("양력"),
    LUNAR("음력");

    private final String description;

    CalendarType(String description) {
        this.description = description;
    }

}
