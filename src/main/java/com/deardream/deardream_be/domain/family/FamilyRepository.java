package com.deardream.deardream_be.domain.family;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FamilyRepository extends JpaRepository<Family, Long> {

    @Query("SELECT f.id FROM Family f")
    List<Long> findAllFamilyIds();
}
