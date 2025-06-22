package com.deardream.deardream_be.domain.user;

public enum Relation {
    FATHER("아버지"),
    MOTHER("어머니"),
    SON("아들"),
    DAUGHTER("딸"),
    BROTHER("형제"),
    SISTER("자매"),
    GRAND_FATHER("할아버지"),
    GRAND_MOTHER("할머니"),
    UNCLE("삼촌"),
    AUNT("이모/고모"),
    COUSIN("사촌"),
    FRIEND("친구"),
    OTHER("기타");

    private final String description;

    Relation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
