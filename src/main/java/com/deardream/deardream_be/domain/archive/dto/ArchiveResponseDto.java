package com.deardream.deardream_be.domain.archive.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ArchiveResponseDto {
    private String yearMonthType;
    private String pdfUrl;
}
