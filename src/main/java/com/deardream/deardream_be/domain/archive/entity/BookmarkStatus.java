package com.deardream.deardream_be.domain.archive.entity;

import lombok.Getter;

@Getter
public enum BookmarkStatus {
    BOOKMARKED("북마크됨"),
    NOT_BOOKMARKED("북마크 해제됨");

    private final String description;

    BookmarkStatus(String description) {
        this.description = description;
    }
}
