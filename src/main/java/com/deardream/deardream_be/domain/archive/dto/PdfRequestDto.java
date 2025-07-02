package com.deardream.deardream_be.domain.archive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfRequestDto {
    private int year;
    private int month;
    private Long familyId;
}
