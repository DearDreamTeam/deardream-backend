package com.deardream.deardream_be.domain.archive.repository;

import com.deardream.deardream_be.domain.archive.MonthlyArchive;
import com.deardream.deardream_be.domain.family.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArchiveRepository extends JpaRepository<MonthlyArchive, Long> {
    List<MonthlyArchive> findAllByFamily(Family family);
}
