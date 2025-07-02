package com.deardream.deardream_be.domain.archive.repository;

import com.deardream.deardream_be.domain.archive.MonthlyArchive;
import com.deardream.deardream_be.domain.family.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.YearMonth;

public interface ArchiveRepository extends JpaRepository<MonthlyArchive, Long> {
    boolean existsByFamilyAndMonthYear(Family family, YearMonth monthYear);
}
