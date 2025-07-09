package com.deardream.deardream_be.domain.user.repository;

import com.deardream.deardream_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 1) 카카오 아이디로 찾기
    Optional<User> findByKakaoId(Long kakaoId);


}
