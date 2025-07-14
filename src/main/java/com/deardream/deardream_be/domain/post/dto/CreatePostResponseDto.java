package com.deardream.deardream_be.domain.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreatePostResponseDto {
    Long postId;
    int postCounts;
}
