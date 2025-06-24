package com.deardream.deardream_be.domain.post.repository;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByFamily(Family family);
}
