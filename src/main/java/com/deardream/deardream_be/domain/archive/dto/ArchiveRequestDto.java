package com.deardream.deardream_be.domain.archive.dto;

import com.deardream.deardream_be.domain.user.Relation;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ArchiveRequestDto {
    @NotNull
    private String authorName;

    @NotNull
    private Relation relation;

    private String content;

    private LocalDateTime createdAt;

    private List<String> imageUrls;
}
