package com.deardream.deardream_be.domain.recipient.repository;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.institution.Institution;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    Optional<Recipient> findByFamilyId(Long familyId);
    List<Recipient> findAllByInstitution(Institution institution);
    Optional<Recipient> findByLeaderId(Long leaderId);
    boolean existsByLeaderId(Long leaderId);

    void deleteAllByFamily(Family family);
    @Query("SELECT r FROM Recipient r WHERE r.family.id =:familyId")
    Optional<Recipient> findSingleByFamilyId(@Param("familyId") Long familyId);
}
