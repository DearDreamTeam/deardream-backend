package com.deardream.deardream_be.domain.post.dto;

import com.deardream.deardream_be.domain.user.Relation;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostResponseDto {
    private Long postId;
    private Long authorId;
    private List<String> imageUrls;
    private String authorProfileImg;
    private String relations;
    private String authorName;
    private String content;
    // 이건 추훙에 들어가할 사항
    private LocalDateTime createdAt;

}
