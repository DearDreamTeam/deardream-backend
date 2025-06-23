package com.deardream.deardream_be.post;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.domain.post.Post;
import com.deardream.deardream_be.domain.post.PostImage;
import com.deardream.deardream_be.domain.post.dto.PostRequestDto;
import com.deardream.deardream_be.domain.post.repository.PostImageRepository;
import com.deardream.deardream_be.domain.post.repository.PostRepository;
import com.deardream.deardream_be.domain.post.service.PostImageService;
import com.deardream.deardream_be.domain.post.service.PostService;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.config.S3Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostImageService postImageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private S3Config s3Config;


    @Test
    void createPost() throws Exception {
        // Given
        // Mock the necessary dependencies and their behaviors here
        String content = "안녕하세요 이것은 테스트에 들어갈 문장들입니다. 모두들 잘 지내시나요?";
        Long authorId = 1L;
        User author = User.builder().name("John").family(new Family()).build();
        ReflectionTestUtils.setField(author, "id", authorId);
        PostRequestDto request = PostRequestDto.builder()
                .content(content)
                .authorId(authorId)
                .build();

        MultipartFile mockFile = new MockMultipartFile("image.jpg", new byte[10]);
        List<MultipartFile> images = List.of(mockFile);

        given(userRepository.findById(authorId)).willReturn(Optional.of(author));
        given(s3Config.getPostImagesFolder()).willReturn("post-images");
        given(postImageService.uploadFile(any(), any(), any())).willReturn("s3-url");

        // When
        Long postId = postService.createPost(request, images);

        // Then
        verify(postRepository).save(any(Post.class));
        verify(postImageRepository, times(1)).save(any(PostImage.class));
    }
}
