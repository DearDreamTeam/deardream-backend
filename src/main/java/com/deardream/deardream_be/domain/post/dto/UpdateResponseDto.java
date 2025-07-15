package com.deardream.deardream_be.domain.post.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UpdateResponseDto {
    String authorName;
    String content;
    List<String> imageUrls;
    LocalDateTime createdAt;
}
