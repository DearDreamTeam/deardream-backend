package com.deardream.deardream_be.domain.post.controller;

import com.deardream.deardream_be.domain.jwt.CustomUserDetails;
import com.deardream.deardream_be.domain.post.dto.*;
import com.deardream.deardream_be.domain.post.service.PostService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.apiPayload.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.attribute.standard.Media;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;


    @Operation(summary = "게시글을 생성합니다. 사진은 0, 1, 2 리스트로 넣어주세요.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CreatePostResponseDto> createPost(
            @Parameter(
                    description = "게시글 본문(JSON)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostRequestDto.class)
                    )
            )
            @RequestPart("request") PostRequestDto request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        CreatePostResponseDto response = postService.createPost(request, images != null ? images : List.of());
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "게시글을 생성합니다, 이 경우 사진은 0 또는 1개 입니다.")
    @PostMapping(
            value = "/test",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<Long> createTestPost(
            @Parameter(description = "게시글 내용", example = "오늘 날씨가 좋아요.")
            @RequestParam("content") String content,

            @Parameter(description = "작성자 ID", example = "11")
            @RequestParam("authorId") Long authorId,

            @RequestPart(value = "image", required = false)
            MultipartFile image
    ) {
        PostRequestDto request = PostRequestDto.builder()
                .content(content)
                .authorId(authorId)
                .build();

        Long postId =postService.createTestPost(request, image);
        return ApiResponse.onSuccess(postId);
    }


    // 나중에 로그인 완료 시 토큰에서 user 추출 필요
    @Operation(summary = "게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public void deletePost(
            @RequestParam Long userId,
            @PathVariable Long postId) {
        postService.deletePost(userId, postId);
    }

    // test 필요
    // 나중에 로그인 완료 시 토큰에서 추출할 예정 - 리펙토링 필요
    @Operation(summary = "게시글을 수정합니다.")
    @PutMapping("/{postId}")
    public ApiResponse<UpdateResponseDto> updatePost(
            @PathVariable Long postId,
            @RequestPart PostUpdateDto request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        ;
        return ApiResponse.onSuccess(postService.updatePost(postId, request, images));
    }

    @PatchMapping("/{postId}")
    public ApiResponse<UpdateResponseDto> updatePostWithImages(
        @PathVariable Long postId,
        @RequestPart PatchPostDto request,
        @RequestPart(value = "images", required = false) List<MultipartFile> images
    ){
        return ApiResponse.onSuccess(postService.patchPost(postId, request, images));
    }

    @Operation(summary = "가족의 게시글을 조회합니다.")
    @GetMapping("/{familyId}")
    public ApiResponse<List<PostResponseDto>> getPosts(
            @PathVariable Long familyId
    ) {
        List<PostResponseDto> response =  postService.getPosts(familyId);
        return ApiResponse.onSuccess(response);
    }
}
