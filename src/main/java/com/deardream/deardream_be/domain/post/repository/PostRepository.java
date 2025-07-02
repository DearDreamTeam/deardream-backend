package com.deardream.deardream_be.domain.post.repository;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByFamily(Family family);


    @Query("SELECT p FROM Post p WHERE p.family.id = :familyId AND p.createdAt BETWEEN :start AND :end")
    List<Post> findPostsForMonth(@Param("familyId") Long familyId,
                                 @Param("start") LocalDate start,
                                 @Param("end") LocalDate end);
}
