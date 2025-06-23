package com.deardream.deardream_be.domain.post.service;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.domain.post.Post;
import com.deardream.deardream_be.domain.post.PostImage;
import com.deardream.deardream_be.domain.post.dto.PostRequestDto;
import com.deardream.deardream_be.domain.post.dto.PostResponseDto;
import com.deardream.deardream_be.domain.post.dto.PostUpdateDto;
import com.deardream.deardream_be.domain.post.repository.PostImageRepository;
import com.deardream.deardream_be.domain.post.repository.PostRepository;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.deardream.deardream_be.global.config.S3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostImageRepository postImageRepository;
    private final PostRepository postRepository;
    private final PostImageService postImageService;
    private final S3Config s3Config;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;

    @Transactional
    public Long createPost(PostRequestDto request, List<MultipartFile> imageFiles) {

        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 게시글 저장
        Post post = Post.builder()
                .author(author)
                .content(request.getContent())
                .family(author.getFamily())
                .build();

        postRepository.save(post);

        // 이미지가 있다면 업로드 (최대 2장)
        if(imageFiles != null && !imageFiles.isEmpty()) {
            if(imageFiles.size() > 2) {
                throw new GeneralException(ErrorStatus._IMAGE_ONLY_TWO);
            }

            for (MultipartFile image : imageFiles) {
                String uuid = UUID.randomUUID().toString();
                String fileName = uuid + "-" + image.getOriginalFilename();

                String s3Url = postImageService.uploadFile(s3Config.getPostImagesFolder(), fileName, image);

                PostImage postImage = PostImage.builder()
                        .post(post)
                        .s3Key(s3Config.getPostImagesFolder() + "/" + fileName)
                        .fileName(image.getOriginalFilename())
                        .s3Url(s3Url)
                        .build();
                postImageRepository.save(postImage);
            }
        }
        return post.getId();
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._POST_NOT_FOUND));

        List<PostImage> images = postImageRepository.findByPost(post);
        for(PostImage image :images) {
            postImageService.deleteFile(image.getS3Key());
        }

        postImageRepository.deleteAll();
        postRepository.delete(post);
    }

    @Transactional
    public void updatePost(Long postId, PostUpdateDto request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._POST_NOT_FOUND));

        // 게시글 내용 수정
        post.updateContent(request.getContent());

        // 2. 안전한 방식으로 기존 이미지 삭제 후 새 이미지 업로드 방식으로 진행하였습니다.
        List<PostImage> existingImages = postImageRepository.findByPost(post);
        for (PostImage image : existingImages) {
            postImageService.deleteFile(image.getS3Key());
        }
        postImageRepository.deleteAll(existingImages);

        List<MultipartFile> newImages = request.getMultipartFiles();

        // 3. 새 이미지가 있다면 업로드
        if(newImages != null && !newImages.isEmpty()) {
            if(newImages.size() > 2) {
                throw new GeneralException(ErrorStatus._IMAGE_ONLY_TWO);
            }

            for(MultipartFile image : newImages) {
                String uuid = UUID.randomUUID().toString();
                String fileName = uuid + "-" + image.getOriginalFilename();
                String s3Key = s3Config.getPostImagesFolder() + "/" + fileName;

                postImageService.uploadFile(s3Config.getPostImagesFolder(),fileName,image);
                PostImage postImage = PostImage.builder()
                        .post(post)
                        .s3Key(s3Key)
                        .fileName(image.getOriginalFilename())
                        .build();

                postImageRepository.save(postImage);
            }
        }
    }

    public List<PostResponseDto> getPosts(Long familyId) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));



        List<Post> posts = postRepository.findAllByFamily(family);

        return posts.stream().map(post -> {
            List<String> imageUrls = postImageRepository.findByPost(post).stream()
                    .map(image -> postImageService.getPreSignedUrl(image.getS3Key()))
                    .toList();

            return PostResponseDto.builder()
                    .postId(post.getId())
                    .authorId(post.getAuthor().getId())
                    .authorName(post.getAuthor().getName())
                    .relations(post.getAuthor().getRelation())
                    .content(post.getContent())
                    .createdAt(post.getCreatedAt())
                    .imageUrls(imageUrls)
                    .build();
        }).collect(Collectors.toList());
    }

}
