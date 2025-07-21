package com.deardream.deardream_be.domain.post.service;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.post.Post;
import com.deardream.deardream_be.domain.post.PostImage;
import com.deardream.deardream_be.domain.post.dto.*;
import com.deardream.deardream_be.domain.post.repository.PostImageRepository;
import com.deardream.deardream_be.domain.post.repository.PostRepository;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.deardream.deardream_be.global.common.UploadResult;
import com.deardream.deardream_be.global.config.S3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostService {

    private final PostImageRepository postImageRepository;
    private final PostRepository postRepository;
    private final PostImageService postImageService;
    private final S3Config s3Config;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;

    @Transactional
    public CreatePostResponseDto createPost(PostRequestDto request, List<MultipartFile> imageFiles) {

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

                if(image == null || image.isEmpty() || image.getOriginalFilename() == null) {
                    continue; // 이미지가 없으면 건너뜀
                }

                // 이미지가 일정 사이즈 이상일 경우 업로드 불가
                long maxSizeBytes = 1024 * 1024; // 1MB
                if(image.getSize() > maxSizeBytes) {
                    throw new GeneralException(ErrorStatus._IMAGE_SIZE_EXCEEDED);
                }


                Long familyId = author.getFamily().getId();
                String fileName = familyId + image.getOriginalFilename();

                UploadResult result = postImageService.uploadFile(s3Config.getPostImagesFolder(), fileName, image);

                PostImage postImage = PostImage.builder()
                        .post(post)
                        .s3Key(result.getKey())
                        .fileName(image.getOriginalFilename())
                        .s3Url(result.getUrl())
                        .build();
                postImageRepository.save(postImage);
            }
        }

        int postCount = countPosts(author.getFamily().getId());

        CreatePostResponseDto responseDto = CreatePostResponseDto.builder()
                .postId(post.getId())
                .postCounts(postCount)
                .build();

        return responseDto;
    }

    // 사진이 1개이거나 없을 경우 게시글 저장 테스트 서비스
    @Transactional
    public Long createTestPost(PostRequestDto request, MultipartFile image) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 게시글 저장
        Post post = Post.builder()
                .author(author)
                .content(request.getContent())
                .family(author.getFamily())
                .build();

        postRepository.save(post);

        log.info("Is PostImage Null?: " + (image!=null && image.isEmpty()));

        // 이미지가 있다면 업로드 (최대 2장)
        // 헐 항상 image!=null 부터 체크해야 함.. 당연하지만 ㅋ
        if (image!=null && !image.isEmpty()) {

            log.info("Image file name: {}", image.getOriginalFilename());

            // 이미지가 일정 사이즈 이상일 경우 업로드 불가
            long maxSizeBytes = 1024 * 1024; // 1MB
            if (image.getSize() > maxSizeBytes) {
                throw new GeneralException(ErrorStatus._IMAGE_SIZE_EXCEEDED);
            }


            Long familyId = author.getFamily().getId();
            String fileName = familyId + post.getId() + image.getOriginalFilename();

            UploadResult result = postImageService.uploadFile(s3Config.getPostImagesFolder(), fileName, image);


            log.info("Image upload result: {}", result);

            PostImage postImage = PostImage.builder()
                    .post(post)
                    .s3Key(result.getKey())
                    .fileName(image.getOriginalFilename())
                    .s3Url(result.getUrl())
                    .build();
            postImageRepository.save(postImage);
        }

        return post.getId();
    }

    @Transactional
    public void createImage(MultipartFile image) {

    }


    @Transactional
    public void deletePost(Long authorId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._POST_NOT_FOUND));

        if(!Objects.equals(post.getAuthor().getId(), authorId)) {
            throw new GeneralException(ErrorStatus._NOT_AUTHOR_OF_POST);
        }

        List<PostImage> images = postImageRepository.findByPost(post);
        for(PostImage image :images) {
            postImageService.deleteFile(image.getS3Key());
            postImageRepository.delete(image);
        }
        postRepository.delete(post);
    }

    @Transactional
    public UpdateResponseDto updatePost(Long postId, PostUpdateDto request, List<MultipartFile> images) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._POST_NOT_FOUND));

        // 나중에 로그인 완료 시 userId는 토큰에서 추출하도록 변경 예정
        if(!Objects.equals(post.getAuthor().getId(), request.getAuthorId())) {
            throw new GeneralException(ErrorStatus._AUTHORITY_NOT_MATCH);
        }

        // 게시글 내용 수정
        post.updateContent(request.getContent());

        // 2. 안전한 방식으로 기존 이미지 삭제 후 새 이미지 업로드 방식으로 진행하였습니다.
        List<PostImage> existingImages = postImageRepository.findByPost(post);
        for (PostImage image : existingImages) {
            postImageService.deleteFile(image.getS3Key());
            postImageRepository.delete(image);
        }

        // List<MultipartFile> newImages = images;

        // 3. 새 이미지가 있다면 업로드
        if(images != null && !images.isEmpty()) {
            if(images.size() > 2) {
                throw new GeneralException(ErrorStatus._IMAGE_ONLY_TWO);
            }

            for(MultipartFile image : images) {
                String fileName = post.getFamily().getId() + image.getOriginalFilename();

                UploadResult result =  postImageService.uploadFile(s3Config.getPostImagesFolder(),fileName,image);
                PostImage postImage = PostImage.builder()
                        .post(post)
                        .s3Key(result.getKey())
                        .s3Url(result.getUrl())
                        .fileName(image.getOriginalFilename())
                        .build();

                postImageRepository.save(postImage);
            }
        }

        UpdateResponseDto response = UpdateResponseDto.builder()
                .authorName(post.getAuthor().getName())
                .content(post.getContent())
                .imageUrls(postImageRepository.findByPost(post).stream()
                        .map(image -> postImageService.getFilesUrl(image.getS3Key()))
                        .collect(Collectors.toList()))
                .createdAt(post.getCreatedAt())
                .build();

        return response;
    }

    @Transactional
    public UpdateResponseDto patchPost(Long postId, PatchPostDto request, List<MultipartFile> images) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._POST_NOT_FOUND));

        // 나중에 로그인 완료 시 userId는 토큰에서 추출하도록 변경 예정
        if(!Objects.equals(post.getAuthor().getId(), request.getAuthorId())) {
            throw new GeneralException(ErrorStatus._AUTHORITY_NOT_MATCH);
        }

        // 게시글 내용 수정
        post.updateContent(request.getContent());

        // 2. 기존 이미지들 가져오기
        List<PostImage> existingImages = postImageRepository.findByPost(post);

        // 3. 유지하고 싶은 이미지 URL 리스트 받기
        List<String> keepImageUrls = request.getExistingImageUrls() != null
                ? request.getExistingImageUrls()
                : new ArrayList<>();

        // 4. 기존 이미지 중 삭제 대상만 삭제
        for (PostImage image : existingImages) {
            String url = postImageService.getFilesUrl(image.getS3Key());

            // 프론트에서 유지하겠다는 URL이 아닌 경우 삭제
            if (!keepImageUrls.contains(url)) {
                postImageService.deleteFile(image.getS3Key());
                postImageRepository.delete(image);
            }
        }

        // 5. 새 이미지 업로드 (최대 2장 제한: 기존 + 새 이미지 합 기준)
        int currentImageCount = keepImageUrls.size();
        int newImageCount = (images != null) ? images.size() : 0;

        if (currentImageCount + newImageCount > 2) {
            throw new GeneralException(ErrorStatus._IMAGE_ONLY_TWO);
        }

        if (images != null) {
            for (MultipartFile image : images) {
                String fileName = post.getFamily().getId() + image.getOriginalFilename();

                UploadResult result = postImageService.uploadFile(
                        s3Config.getPostImagesFolder(), fileName, image
                );

                PostImage postImage = PostImage.builder()
                        .post(post)
                        .s3Key(result.getKey())
                        .s3Url(result.getUrl())
                        .fileName(image.getOriginalFilename())
                        .build();

                postImageRepository.save(postImage);
            }
        }

        // 6. 응답 객체 생성
        UpdateResponseDto response = UpdateResponseDto.builder()
                .authorName(post.getAuthor().getName())
                .content(post.getContent())
                .imageUrls(postImageRepository.findByPost(post).stream()
                        .map(img -> postImageService.getFilesUrl(img.getS3Key()))
                        .collect(Collectors.toList()))
                .createdAt(post.getCreatedAt())
                .build();

        return response;
    }

    public List<PostResponseDto> getPosts(Long familyId) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));



        List<Post> posts = postRepository.findAllByFamily(family);

        return posts.stream().map(post -> {
            List<String> imageUrls = postImageRepository.findByPost(post).stream()
                    .map(image -> postImageService.getFilesUrl(image.getS3Key()))
                    .toList();

            return PostResponseDto.builder()
                    .postId(post.getId())
                    .authorId(post.getAuthor().getId())
                    .authorName(post.getAuthor().getName())
                    .relations(post.getAuthor().getRelation().getDescription())
                    .content(post.getContent())
                    .createdAt(post.getCreatedAt())
                    .imageUrls(imageUrls)
                    .authorProfileImg(post.getAuthor().getProfileImage())
                    .build();
        }).collect(Collectors.toList());
    }

    public List<PostResponseDto> getPostsByYearMonth(Long familyId, int year, int month) {

        List<Post> posts = postRepository.findByFamilyIdAndYearAndMonth(familyId, year, month);

        return posts.stream().map(post -> {
            List<String> imageUrls = postImageRepository.findByPost(post).stream()
                    .map(image -> postImageService.getFilesUrl(image.getS3Key()))
                    .toList();

            return PostResponseDto.builder()
                    .postId(post.getId())
                    .authorId(post.getAuthor().getId())
                    .authorName(post.getAuthor().getName())
                    .relations(post.getAuthor().getRelation().getDescription())
                    .content(post.getContent())
                    .createdAt(post.getCreatedAt())
                    .imageUrls(imageUrls)
                    .build();
        }).collect(Collectors.toList());
    }

    // 가족 당 한 달 post의 개수는 최대 20개
    private int countPosts(Long familyId) {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        return postRepository.countByFamilyAndCreatedAtBetween(family,
                LocalDateTime.of(now.getYear(), now.getMonth(), 1, 0, 0),
                LocalDateTime.of(now.getYear(), now.getMonth(), now.lengthOfMonth(), 23, 59, 59));

    }

    public Optional<String> getRandomThumbnailUrl(Family family, int year, int month) {
        List<Post> posts = postRepository.findByFamilyIdAndYearAndMonth(family.getId(), year, month);

        List<String> imageUrls = posts.stream()
                .flatMap(post -> postImageRepository.findByPost(post).stream()
                        .map(image -> postImageService.getFilesUrl(image.getS3Key())))
                .toList();

        if (imageUrls.isEmpty()) return Optional.empty();

        // 랜덤하게 하나 뽑기
        return Optional.of(imageUrls.get(new Random().nextInt(imageUrls.size())));
    }

}
