package com.deardream.deardream_be.domain.archive.repository;

import com.deardream.deardream_be.domain.archive.entity.MonthlyArchive;
import com.deardream.deardream_be.domain.family.Family;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Month;
import java.util.List;

public interface ArchiveRepository extends JpaRepository<MonthlyArchive, Long> {
    List<MonthlyArchive> findAllByFamily(Family family);

    List<MonthlyArchive> findAllByArchiveYearAndArchiveMonth(int archiveYear, int archiveMonth);

    @Query("""
    SELECT ma FROM MonthlyArchive ma
    JOIN ma.family f
    JOIN Recipient r ON r.family = f
    WHERE r.code.code = :institutionId
      AND ma.archiveYear = :year
      AND ma.archiveMonth = :month
""")
    List<MonthlyArchive> findArchivesByInstitutionIdAndYearMonth(
            @Param("institutionId") Long institutionId,
            @Param("year") int year,
            @Param("month") int month
    );

}
