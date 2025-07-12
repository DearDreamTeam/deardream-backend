package com.deardream.deardream_be.domain.user;

public enum Relation {
    SON("아들"),
    DAUGHTER("딸"),
    GRANDSON("손자"),
    GRANDDAUGHTER("손녀"),
    SPOUSE("배우자"),
    BROTHER("형제"),
    SISTER("자매"),
    OTHER("기타");

    private final String description;

    Relation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
