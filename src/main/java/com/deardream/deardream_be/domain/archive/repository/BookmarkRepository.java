package com.deardream.deardream_be.domain.archive.repository;

import com.deardream.deardream_be.domain.archive.entity.ArchiveBookmark;
import com.deardream.deardream_be.domain.archive.entity.MonthlyArchive;
import com.deardream.deardream_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<ArchiveBookmark, Long> {

    Optional<ArchiveBookmark> findByUserAndArchive(User user, MonthlyArchive archive);
    List<ArchiveBookmark> findAllByUser(User user);
}
