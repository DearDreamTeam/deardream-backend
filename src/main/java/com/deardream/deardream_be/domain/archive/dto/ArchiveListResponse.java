package com.deardream.deardream_be.domain.archive.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ArchiveListResponse {
    private List<ArchiveResponseDto> dtos;
}
