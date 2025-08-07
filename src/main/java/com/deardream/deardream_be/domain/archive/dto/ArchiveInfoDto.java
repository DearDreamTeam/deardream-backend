package com.deardream.deardream_be.domain.archive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ArchiveInfoDto {
    private int year;
    private int month;
    private String recipientName;
    private List<AuthorInfoDto> authors;

    @Getter
    @Builder
    public static class AuthorInfoDto {
        private String authorName;
        private String relation;
    }

}
