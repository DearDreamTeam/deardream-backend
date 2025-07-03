package com.deardream.deardream_be.domain.archive.repository;

import com.deardream.deardream_be.domain.archive.entity.MonthlyArchive;
import com.deardream.deardream_be.domain.family.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchiveRepository extends JpaRepository<MonthlyArchive, Long> {
    List<MonthlyArchive> findAllByFamily(Family family);
}
