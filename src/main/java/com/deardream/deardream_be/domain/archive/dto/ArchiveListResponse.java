package com.deardream.deardream_be.domain.archive.dto;

import lombok.Builder;

import java.util.List;

@Builder
public class ArchiveListResponse {
    private int count;
    private List<ArchiveResponseDto> dtos;
}
