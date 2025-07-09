package com.deardream.deardream_be.domain.user;

import lombok.Getter;

@Getter
public enum Role {
    LEADER("대표자"),
    USER("사용자"),
    ADMIN("관리자");

    private final String description;

    Role(String description) {this.description = description;}
}
