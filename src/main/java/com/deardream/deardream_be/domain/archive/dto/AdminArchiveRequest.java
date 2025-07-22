package com.deardream.deardream_be.domain.archive.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminArchiveRequest {
    private int year;
    private int month;
}
