package com.deardream.deardream_be.domain.institution;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    Optional<Institution> findByCode(String code);

    boolean existsByCode(String code);
    boolean existsByNameAndAddress(String name, String address);

}
