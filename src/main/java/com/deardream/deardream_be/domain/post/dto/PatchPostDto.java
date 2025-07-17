package com.deardream.deardream_be.domain.post.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PatchPostDto {
    private Long authorId;
    private String content;
    private List<String> existingImageUrls;
}
