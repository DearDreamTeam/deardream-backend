//package com.deardream.deardream_be.user;
//
//import com.deardream.deardream_be.domain.user.dto.UserRequestDto;
//import com.deardream.deardream_be.domain.user.dto.UserResponseDto;
//import com.deardream.deardream_be.domain.user.entity.User;
//import com.deardream.deardream_be.domain.user.repository.UserRepository;
//import com.deardream.deardream_be.domain.user.service.UserServiceImplementation;
//import com.deardream.deardream_be.domain.user.Relation;
//import com.deardream.deardream_be.domain.institution.CalendarType;
//import com.deardream.deardream_be.domain.user.Role;
//import jakarta.persistence.EntityNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.BDDMockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceUnitTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private UserServiceImplementation userService;
//
//    private User existingUser;
//
//    @BeforeEach
//    void setUp() {
//        // 테스트용 User 엔티티 준비 (카카오 로그인 후 기본 정보만 들어있다고 가정)
//        existingUser = User.builder()
//                .kakaoId(123L)
//                .name("초기이름")
//                .email("test@example.com")
//                .build();
//        // id, createdAt 은 리플렉션으로 세팅
//        ReflectionTestUtils.setField(existingUser, "id", 42L);
//        ReflectionTestUtils.setField(existingUser, "createdAt", LocalDateTime.now());
//    }
//
//    @Test
//    void register_updates_entity_and_returns_responseDto() {
//        // given
//        long kakaoId = 123L;
//        UserRequestDto dto = UserRequestDto.builder()
//                .name("홍길동")
//                .calendarType(CalendarType.SOLAR)
//                .birth(LocalDate.of(1990, 5, 1))
//                .relation(Relation.DAUGHTER)
//                .otherRelation(null)
//                .profileImage("profile.jpg")
//                .build();
//
//        given(userRepository.findByKakaoId(kakaoId))
//                .willReturn(Optional.of(existingUser));
//
//        // when
//        UserResponseDto response = userService.register(kakaoId, dto);
//
//        // then: entity.updateInfo() 가 호출되어 이름, 생일 등이 반영된다
//        assertThat(response.getId()).isEqualTo(42L);
//        assertThat(response.getName()).isEqualTo("홍길동");
//        assertThat(response.getCalendarType()).isEqualTo(CalendarType.SOLAR);
//        assertThat(response.getBirth()).isEqualTo(LocalDate.of(1990, 5, 1));
//        assertThat(response.getRelation()).isEqualTo(Relation.DAUGHTER);
//        assertThat(response.getProfileImage()).isEqualTo("profile.jpg");
//
//        then(userRepository).should(times(1)).findByKakaoId(kakaoId);
//    }
//
//    @Test
//    void getMyInfo_returns_responseDto_when_found() {
//        // given
//        long userId = 42L;
//        given(userRepository.findById(userId))
//                .willReturn(Optional.of(existingUser));
//
//        // when
//        UserResponseDto info = userService.getMyInfo(userId);
//
//        // then
//        assertThat(info.getId()).isEqualTo(42L);
//        assertThat(info.getName()).isEqualTo("초기이름");
//        then(userRepository).should().findById(userId);
//    }
//
//    @Test
//    void getMyInfo_throws_when_not_found() {
//        // given
//        given(userRepository.findById(999L)).willReturn(Optional.empty());
//
//        // when / then
//        assertThatThrownBy(() -> userService.getMyInfo(999L))
//                .isInstanceOf(EntityNotFoundException.class)
//                .hasMessageContaining("존재하지 않는 회원");
//    }
//}
