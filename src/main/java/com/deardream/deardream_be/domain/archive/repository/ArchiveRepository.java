package com.deardream.deardream_be.domain.archive.repository;

import com.deardream.deardream_be.domain.archive.entity.MonthlyArchive;
import com.deardream.deardream_be.domain.family.entity.Family;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Month;
import java.util.List;
import java.util.Optional;

public interface ArchiveRepository extends JpaRepository<MonthlyArchive, Long> {
    List<MonthlyArchive> findAllByFamily(Family family);

    List<MonthlyArchive> findAllByArchiveYearAndArchiveMonth(int archiveYear, int archiveMonth);

    Optional<MonthlyArchive> findByFamilyAndArchiveYearAndArchiveMonth(Family family, int archiveYear, int archiveMonth);

    @Query("""
    SELECT ma FROM MonthlyArchive ma
    JOIN ma.family f
    JOIN Recipient r ON r.family = f
    WHERE r.institution.code = :institutionId
      AND ma.archiveYear = :year
      AND ma.archiveMonth = :month
""")
    List<MonthlyArchive> findtestArchivesByInstitutionIdAndYearMonth(
            @Param("institutionId") Long institutionId,
            @Param("year") int year,
            @Param("month") int month
    );

    @Query("""
    SELECT ma FROM MonthlyArchive ma
    JOIN ma.family f
    JOIN Recipient r ON r.family = f
    WHERE r.institution.code = :institutionCode
      AND ma.archiveYear = :year
      AND ma.archiveMonth = :month
""")
    List<MonthlyArchive> findArchivesByInstitutionCodeAndYearMonth(
            @Param("institutionCode") String institutionCode,
            @Param("year") int year,
            @Param("month") int month
    );


    @Query("SELECT a FROM MonthlyArchive a WHERE a.archiveYear = :year AND a.archiveMonth = :month AND a.recipient.deliveryType = 'HOME'")
    List<MonthlyArchive> findHomeArchives(int year, int month);

    void deleteAllByFamily(Family family);
}
