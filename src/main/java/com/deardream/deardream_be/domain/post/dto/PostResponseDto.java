package com.deardream.deardream_be.domain.post.dto;

import com.deardream.deardream_be.domain.user.Relation;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostResponseDto {
    private Long postId;
    private String content;
    private List<String> imageUrls;
    private Long authorId;
    private String authorName;
    private Relation relations;
    private LocalDateTime createdAt;

}
