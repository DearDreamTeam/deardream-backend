package com.deardream.deardream_be.domain.archive.dto;

import com.deardream.deardream_be.domain.user.Relation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveRequest {
    private String profileImageUrl;
    @NotNull
    private String authorName;
    @NotNull
    private Relation relationShip;

    private List<String> postImages;
    private String content;
    private LocalDateTime createdDate;
}
