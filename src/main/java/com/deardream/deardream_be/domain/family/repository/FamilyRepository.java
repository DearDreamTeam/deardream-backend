package com.deardream.deardream_be.domain.family.repository;

import com.deardream.deardream_be.domain.family.entity.Family;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
//@Profile("!prod")
public interface FamilyRepository extends JpaRepository<Family, Long> {

    @Query("SELECT f.id FROM Family f")
    List<Long> findAllFamilyIds();
    Optional<Family> findByLeaderId(Long leadrId);
//    Optional<Family> findFamilyByLink(String link);
    Optional<Family> findByFamilyLink(String inviteCode);
}
