package com.deardream.deardream_be.domain.recipient.repository;

import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    Optional<Recipient> findByFamilyId(Long familyId);
}
