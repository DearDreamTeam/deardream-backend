package com.deardream.deardream_be.domain.post.repository;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByFamily(Family family);

    @Query("SELECT p FROM Post p WHERE p.family.id = :familyId AND YEAR(p.createdAt) = :year AND MONTH(p.createdAt) = :month")
    List<Post> findByFamilyIdAndYearAndMonth(@Param("familyId") Long familyId,
                                             @Param("year") int year,
                                             @Param("month") int month);



    @Query("SELECT p FROM Post p WHERE p.family.id = :familyId AND p.createdAt BETWEEN :start AND :end")
    List<Post> findPostsForMonth(@Param("familyId") Long familyId,
                                 @Param("start") LocalDate start,
                                 @Param("end") LocalDate end);

    int countByFamilyAndCreatedAtBetween(Family family, LocalDateTime start, LocalDateTime end);

}
